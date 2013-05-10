import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class recuperateur_de_code extends PApplet {

/*
 R\u00e9cup\u00e9rateur de codes 433.92Mhz
 Auteur: Sarrailh R\u00e9mi (maditnerd)
 License : Gplv3
 http://www.tldrlegal.com/l/GPL3
 
 Description: 
 Ce programme permet de r\u00e9cup\u00e9rer le code renvoy\u00e9 par une t\u00e9l\u00e9commande en 433.92Mhz
 puis de l'enregistrer dans un fichier texte
 
 Ce programme ne marchera pas si le code est prot\u00e9g\u00e9 contre le "Replay" 
 c.a.d le plus souvent, les t\u00e9l\u00e9commandes de portail
 et de voiture (Heuresement!)
 
 Arduino:
 Ce code n\u00e9cessite de programmer l'Arduino avec ce code (\u00e0 mettre)
 et d'y brancher un r\u00e9cepteur 433.92Mhz sur la pin (broche) 2
 
 NECESSITE LA BIBLIOTHEQUE controlP5 (\u00e0 mettre dans librairies dans votre sketchbook)
 http://www.sojamo.de/libraries/controlP5/#installation
 
 */
 
 

 //Biblioth\u00e8que de communication en s\u00e9rie
 // Biblioth\u00e8que pour l'interface

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
                //Je laisse quand m\u00eame le code d\u00e8s fois que quelqu'un se penche dessus

PFont police; //Police d'\u00e9criture
Serial Serial_arduino;  // Connexion au port s\u00e9rie
String readString = ""; // Texte r\u00e9cup\u00e9r\u00e9 sur le port S\u00e9rie
int nbcode = 0; //Nombre de code r\u00e9cup\u00e9r\u00e9
String code_actuel = ""; //Code actuel r\u00e9cup\u00e9r\u00e9
String[] liste_id = new String[100]; //Tableau des ID des T\u00e9l\u00e9commandes
String[] liste_code = new String[100]; //Tableau des code des T\u00e9l\u00e9commandes

////


/*
 Initialisation
*/

public void setup() {
  //On connecte au d\u00e9marrage l'arduino avec l'ordinateur (On part du principe que l'arduino sera sur le derni\u00e8re port s\u00e9rie connect\u00e9)
  Serial_arduino = new Serial(this, Serial.list()[Serial.list().length-1], 9600);

  cp5 = new ControlP5(this); //Gestionnaire d'interface ControlP5
  
  //Creation de la fenetre
  size(largeur_fenetre, hauteur_fenetre); 
  background(color(100,100,100));

  //Creation de la police d'\u00e9criture par d\u00e9faut
  creer_police();

  //Titre
  creer_texte("R\u00e9cup\u00e9ration des codes des t\u00e9l\u00e9commandes",position_hauteur);
  creer_ligne(30);

  //Port s\u00e9rie
  text("Connexion \u00e0",aligner_au_centre - 240, position_hauteur * 12.5f);
  text(Serial.list()[Serial.list().length-1],aligner_au_centre -250,position_hauteur * 14);

  //Champ ID T\u00e9l\u00e9commande
  id = cp5.addTextfield("ID telecommande")
     .setPosition(aligner_au_centre_P5,position_hauteur * 2)
     .setSize(200,40)
     .setFont(police)
     .setFocus(true)
     .setColor(color(255,0,0))
     .setAutoClear(false);
     ;
  
  //Champ Code t\u00e9l\u00e9commande
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
                  .setPosition(aligner_au_centre_P5-30,position_hauteur * 12.5f)
                  .setSize(120,hauteur_fenetre)
                  .setWidth(30)
                  .setItemHeight(10)
                  .setBarHeight(10)
                 ;
  
  
  //Liste de l'ID des T\u00e9l\u00e9commandes
  listbox_id = cp5.addListBox("ID")
                  .setPosition(aligner_au_centre_P5,position_hauteur * 12.5f)
                  .setSize(120,hauteur_fenetre)
                  .setItemHeight(10)
                  .setWidth(100)
                  .setBarHeight(10)
                 ;
                 
  //Liste des Codes T\u00e9l\u00e9commandes
  listbox_code = cp5.addListBox("CODE")
                  .setPosition(aligner_au_centre_P5+100,position_hauteur * 12.5f)
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
     .setPosition(aligner_au_centre_P5+240,position_hauteur * 12.5f)
     .setSize(100,50)
     .setTriggerEvent(Bang.RELEASE)
     ;
     
  message_erreur("");   //Affiche la boite d'erreur
}

/*
Boucle d'affichage
*/

public void draw() {

  //On lit sur le port s\u00e9rie de l'arduino si il est disponible
  while (Serial_arduino.available () > 0) {
    lire_serie();
  }

  //Si le code est fini d'\u00eatre envoyer alors on l'affiche
  if (readString.length() > 0) {
    code_actuel = readString; //R\u00e9cup\u00e8re le code dans la variable
    code.setValue(code_actuel); //Affiche le code
    readString = ""; //Efface le code en m\u00e9moire
    
    java.awt.Toolkit.getDefaultToolkit().beep(); //Son par d\u00e9faut de l'OS
  }
}

//Si une touche est relach\u00e9e
public void keyReleased() {
  
  //Si ENTREE est appuy\u00e9e
  if (keyCode == 10)
  {

  if (code_actuel != "" && code_actuel.length() > longueur_code_min) //On v\u00e9rifie qu'un code a \u00e9t\u00e9 enregistr\u00e9 correctement
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
   
  //Nettoyage des champs texte / message d'erreur / code sauvegard\u00e9
  code.clear();
  id.clear();
  message_erreur("");
  code_actuel = "";

  //Faire le son syst\u00e8me par d\u00e9faut
  java.awt.Toolkit.getDefaultToolkit().beep();
  
}
else
{
  if (code_actuel == "")
  {
  message_erreur("Aucun code detect\u00e9!"); //Si aucun code , afficher cette erreur
  }
  else
  {
  message_erreur("Le code a \u00e9t\u00e9 mal detect\u00e9, r\u00e9essayez svp!"); //Le code est trop court
  }
}
  }
  
}

//Gestion des boutons
public void controlEvent(ControlEvent theEvent)
{
  //Si bouton Sauvegarder appuy\u00e9
  if (theEvent.getController().getName().equals("Sauvegarder"))
  {
    String[] sauvegarde_code = new String[nbcode+1]; //Creation du tableau des valeurs
    sauvegarde_code[0] = "ID;CODE"; //Entete du fichier_texte (cvs)
    
    //Pour chaque code
    for(int i = 0; i < nbcode; i++)
    {
     sauvegarde_code[i+1] = liste_id[i] + ";" + liste_code[i] ; //Sauvegarder id;code
    }
    saveStrings("codes.txt", sauvegarde_code); //Sauvegarde/Cr\u00e9er le fichier
    
    //Affiche le message comme quoi le fichier a \u00e9t\u00e9 cr\u00e9e
    message_erreur("Fichier codes.txt Sauvegard\u00e9!");
  }
  
  //Retirer pour cause de plantage de l'application :-(
  //Si bouton Annuler appuy\u00e9
  //if (theEvent.getController().getName().equals("Annuler"))
  //{
  //V\u00e9rifie qu'il y a encore quelque chose \u00e0 supprimer
  //if (nbcode > 0)
  //{
      //On revient en arri\u00e8re
  //nbcode--;

  //Efface la derni\u00e8re occurrence des listes
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
  //  message_erreur("Impossible d'annuler"); //Affiche un message d'erreur s'il n'y a plus rien \u00e0 supprimer
 // }
  
 // }
  
 
}

/*

Fonctions 

*/

//Cette fonction lit les donn\u00e9es envoy\u00e9es par l'Arduino
public void lire_serie() {
  delay(30); //D\u00e9lai afin de lire enti\u00e8rement le texte retourn\u00e9e par le port s\u00e9rie
  // R\u00e9cup\u00e8re les donn\u00e9es
  if (Serial_arduino.available() > 0) { //Si le port s\u00e9rie est disponible
    char c = Serial_arduino.readChar(); //Lire le caract\u00e8re
    readString += c; //Ajouter un caract\u00e8re \u00e0 la chaine de caract\u00e8re
  }
}

//Cr\u00e9er la police par d\u00e9faut
public void creer_police()
{
  //Cr\u00e9ation de la police d'\u00e9criture
  police = createFont("Arial", 19, true);
  textFont(police);
  //Aligne le texte au centre
  textAlign(CENTER);
}

//Cr\u00e9er un texte align\u00e9e au centre avec la position (en hauteur) donn\u00e9e
  public void creer_texte(String texte,int position)
  {
    fill(200);
    text(texte, aligner_au_centre, position);
  }

//Cr\u00e9er une ligne
  public void creer_ligne(int position)
  {
    line(10, position, largeur_fenetre - 10, position);
  }

//Cr\u00e9er un message d'erreur
public void message_erreur(String texte)
{
effacer_message_erreur();
fill(255,0,0);
text(texte,aligner_au_centre,position_hauteur * 11);
}

//Efface (cr\u00e9er la boite en faite) le message d'erreur
public void effacer_message_erreur()
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
 
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "recuperateur_de_code" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
