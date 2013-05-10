/*
 Récupérateur de codes 433.92Mhz
 Auteur: Sarrailh Rémi (maditnerd)
 License : Gplv3
 http://www.tldrlegal.com/l/GPL3
 
 Description: 
 Ce programme permet de récupérer le code renvoyé par une télécommande en 433.92Mhz
 puis de l'enregistrer dans un fichier texte
 
 Ce programme ne marchera pas si le code est protégé contre le "Replay" 
 c.a.d le plus souvent, les télécommandes de portail
 et de voiture (Heuresement!)
 
 Arduino:
 Ce code nécessite de programmer l'Arduino avec ce code (à mettre)
 et d'y brancher un récepteur 433.92Mhz sur la pin (broche) 2
 
 NECESSITE LA BIBLIOTHEQUE controlP5 (à mettre dans librairies dans votre sketchbook)
 http://www.sojamo.de/libraries/controlP5/#installation
 
 */
 
 

import processing.serial.*; //Bibliothèque de communication en série
import controlP5.*; // Bibliothèque pour l'interface

/////PARAMETRES
int hauteur_fenetre = 800;
int largeur_fenetre = 600;
int aligner_au_centre = largeur_fenetre / 2;
int aligner_au_centre_P5 = largeur_fenetre / 3;
int position_hauteur = 20;
int longueur_code_min = 6;
////

///VARIABLES

//Gestionnaire d'interface utilisateur ControlP5
ControlP5 cp5;

//Champs Texte
controlP5.Textfield id;
controlP5.Textfield code;

//Liste
ListBox listbox_id;
ListBox listbox_code;
ListBox listbox_nbcode;

//Bouton Annuler
//Bang annuler; //Le bouton faisaient planter l'application, impossible d'en trouver la raison
                //Je laisse quand même le code dès fois que quelqu'un se penche dessus

PFont police; //Police d'écriture
Serial Serial_arduino;  // Connexion au port série
String readString = ""; // Texte récupéré sur le port Série
int nbcode = 0; //Nombre de code récupéré
String code_actuel = ""; //Code actuel récupéré
String[] liste_id = new String[100]; //Tableau des ID des Télécommandes
String[] liste_code = new String[100]; //Tableau des code des Télécommandes

////


/*
 Initialisation
*/

void setup() {
  //On connecte au démarrage l'arduino avec l'ordinateur (On part du principe que l'arduino sera sur le dernière port série connecté)
  Serial_arduino = new Serial(this, Serial.list()[Serial.list().length-1], 9600);

  cp5 = new ControlP5(this); //Gestionnaire d'interface ControlP5
  
  //Creation de la fenetre
  size(largeur_fenetre, hauteur_fenetre); 
  background(color(100,100,100));

  //Creation de la police d'écriture par défaut
  creer_police();

  //Titre
  creer_texte("Récupération des codes des télécommandes",position_hauteur);
  creer_ligne(30);

  //Port série
  text("Connexion à",aligner_au_centre - 240, position_hauteur * 12.5);
  text(Serial.list()[Serial.list().length-1],aligner_au_centre -250,position_hauteur * 14);

  //Champ ID Télécommande
  id = cp5.addTextfield("ID telecommande")
     .setPosition(aligner_au_centre_P5,position_hauteur * 2)
     .setSize(200,40)
     .setFont(police)
     .setFocus(true)
     .setColor(color(255,0,0))
     .setAutoClear(false);
     ;
  
  //Champ Code télécommande
  code = cp5.addTextfield("CODE telecommande")
     .setPosition(aligner_au_centre_P5,position_hauteur * 5)
     .setSize(200,40)
     .setFont(police)
     .setColor(color(255,0,0))
     .setAutoClear(false);
     ;
     
  creer_texte("",position_hauteur * 10); //Champ Erreur
  
  //Liste CODE/ID
  
  //Liste du Nombre de codes
  //redraw_listbox(); //Affiche les contours (utile si on veut pouvoir supprimer un champ)
  
  listbox_nbcode = cp5.addListBox("NB")
                  .setPosition(aligner_au_centre_P5-30,position_hauteur * 12.5)
                  .setSize(120,hauteur_fenetre)
                  .setWidth(30)
                  .setItemHeight(10)
                  .setBarHeight(10)
                 ;
  
  
  //Liste de l'ID des Télécommandes
  listbox_id = cp5.addListBox("ID")
                  .setPosition(aligner_au_centre_P5,position_hauteur * 12.5)
                  .setSize(120,hauteur_fenetre)
                  .setItemHeight(10)
                  .setWidth(100)
                  .setBarHeight(10)
                 ;
                 
  //Liste des Codes Télécommandes
  listbox_code = cp5.addListBox("CODE")
                  .setPosition(aligner_au_centre_P5+100,position_hauteur * 12.5)
                  .setSize(120,hauteur_fenetre)
                  .setItemHeight(10)
                  .setBarHeight(10)
                 ;
                        
  //Texte explication
  creer_texte("Appuyer sur ENTREE pour enregistrer ce code",position_hauteur * 9);

  //Bouton Annuler
  //annuler = cp5.addBang("Annuler")
  //   .setPosition(aligner_au_centre_P5-140, position_hauteur * 12.5)
  //   .setSize(100,50)
  //   .setTriggerEvent(Bang.RELEASE)
  //   ;

  //Bouton Sauvegarder
  cp5.addBang("Sauvegarder")
     .setPosition(aligner_au_centre_P5+240,position_hauteur * 12.5)
     .setSize(100,50)
     .setTriggerEvent(Bang.RELEASE)
     ;
     
  message_erreur("");   //Affiche la boite d'erreur
}

/*
Boucle d'affichage
*/

void draw() {

  //On lit sur le port série de l'arduino si il est disponible
  while (Serial_arduino.available () > 0) {
    lire_serie();
  }

  //Si le code est fini d'être envoyer alors on l'affiche
  if (readString.length() > 0) {
    code_actuel = readString; //Récupère le code dans la variable
    code.setValue(code_actuel); //Affiche le code
    readString = ""; //Efface le code en mémoire
    
    java.awt.Toolkit.getDefaultToolkit().beep(); //Son par défaut de l'OS
  }
}

//Si une touche est relachée
void keyReleased() {
  
  //Si ENTREE est appuyée
  if (keyCode == 10)
  {

  if (code_actuel != "" && code_actuel.length() > longueur_code_min) //On vérifie qu'un code a été enregistré correctement
{  
  //Enregistrement des codes/id dans un tableau et dans les listes
  
  //Tableaux
  liste_code[nbcode] = code_actuel;
  liste_id[nbcode] = id.getText();
  
  //Listes
  println(id.getText());
  listbox_nbcode.addItem(String.valueOf(nbcode),0);
  listbox_id.addItem(id.getText(),0);
  listbox_code.addItem(liste_code[nbcode],0);
 
  nbcode++; //On avance d'un code
   
  //Nettoyage des champs texte / message d'erreur / code sauvegardé
  code.clear();
  id.clear();
  message_erreur("");
  code_actuel = "";

  //Faire le son système par défaut
  java.awt.Toolkit.getDefaultToolkit().beep();
  
}
else
{
  if (code_actuel == "")
  {
  message_erreur("Aucun code detecté!"); //Si aucun code , afficher cette erreur
  }
  else
  {
  message_erreur("Le code a été mal detecté, réessayez svp!"); //Le code est trop court
  }
}
  }
  
}

//Gestion des boutons
void controlEvent(ControlEvent theEvent)
{
  //Si bouton Sauvegarder appuyé
  if (theEvent.getController().getName().equals("Sauvegarder"))
  {
    String[] sauvegarde_code = new String[nbcode+1]; //Creation du tableau des valeurs
    sauvegarde_code[0] = "ID;CODE"; //Entete du fichier_texte (cvs)
    
    //Pour chaque code
    for(int i = 0; i < nbcode; i++)
    {
     sauvegarde_code[i+1] = liste_id[i] + ";" + liste_code[i] ; //Sauvegarder id;code
    }
    saveStrings("codes.txt", sauvegarde_code); //Sauvegarde/Créer le fichier
    
    //Affiche le message comme quoi le fichier a été crée
    message_erreur("Fichier codes.txt Sauvegardé!");
  }
  
  //Retirer pour cause de plantage de l'application :-(
  //Si bouton Annuler appuyé
  //if (theEvent.getController().getName().equals("Annuler"))
  //{
  //Vérifie qu'il y a encore quelque chose à supprimer
  //if (nbcode > 0)
  //{
      //On revient en arrière
  //nbcode--;

  //Efface la dernière occurrence des listes
  //listbox_nbcode.removeItem(String.valueOf(nbcode));
  //listbox_id.removeItem(liste_id[nbcode]);
  //listbox_code.removeItem(liste_code[nbcode]);

  //Efface la dernier valeur des tableaux
  //liste_code[nbcode] = "";
  //liste_id[nbcode] = "";
  //redraw_listbox();  
  //}
  
  //else
  //{
  //  message_erreur("Impossible d'annuler"); //Affiche un message d'erreur s'il n'y a plus rien à supprimer
 // }
  
 // }
  
 
}

/*

Fonctions 

*/

//Cette fonction lit les données envoyées par l'Arduino
void lire_serie() {
  delay(30); //Délai afin de lire entièrement le texte retournée par le port série
  // Récupère les données
  if (Serial_arduino.available() > 0) { //Si le port série est disponible
    char c = Serial_arduino.readChar(); //Lire le caractère
    readString += c; //Ajouter un caractère à la chaine de caractère
  }
}

//Créer la police par défaut
void creer_police()
{
  //Création de la police d'écriture
  police = createFont("Arial", 19, true);
  textFont(police);
  //Aligne le texte au centre
  textAlign(CENTER);
}

//Créer un texte alignée au centre avec la position (en hauteur) donnée
  void creer_texte(String texte,int position)
  {
    fill(200);
    text(texte, aligner_au_centre, position);
  }

//Créer une ligne
  void creer_ligne(int position)
  {
    line(10, position, largeur_fenetre - 10, position);
  }

//Créer un message d'erreur
void message_erreur(String texte)
{
effacer_message_erreur();
fill(255,0,0);
text(texte,aligner_au_centre,position_hauteur * 11);
}

//Efface (créer la boite en faite) le message d'erreur
void effacer_message_erreur()
{
fill(255);
rect(0,position_hauteur * 10, largeur_fenetre-1, 30); 
}

//Redessine les listbox
//void redraw_listbox()
//{
//  fill(100);
//  rect(aligner_au_centre_P5 - 37 ,position_hauteur * 11.5, aligner_au_centre_P5 + 70, hauteur_fenetre);
//}
 
