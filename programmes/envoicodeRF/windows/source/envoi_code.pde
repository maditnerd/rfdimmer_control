/*
 Récupérateur de codes 433.92Mhz
 Auteur: Sarrailh Rémi (maditnerd)
 License : Gplv3
 http://www.tldrlegal.com/l/GPL3
 
 Description: 
 Ce code permet d'envoyer des codes à plusieurs LED DIMMER (ou n'importe quoi fonctionnant en 433.92Mhz).
 
 Arduino:
 Ce code nécessite de programmer l'Arduino avec ce code (à mettre)
 et d'y brancher un récepteur 433.92Mhz sur la pin (broche) 2
 
 NECESSITE LA BIBLIOTHEQUE controlP5 (à mettre dans librairies dans votre sketchbook)
 http://www.sojamo.de/libraries/controlP5/#installation

 */

import processing.serial.*; //Bibliothèque de communication en série
import controlP5.*; // Bibliothèque pour l'interface

/////PARAMETRES
int hauteur_fenetre = 210;
int largeur_fenetre = 300;
int aligner_au_centre = largeur_fenetre / 2;
int aligner_au_centre_P5 = largeur_fenetre / 3;
int position_hauteur = 20;
int longueur_code_min = 6;
////

//Gestionnaire d'interface utilisateur ControlP5
ControlP5 cp5;

//Champs Texte
controlP5.Textfield champ_repetition;

PFont police; //Police d'écriture
Serial Serial_arduino;  // Connexion au port série
int nbcode = 0;
int variable = 20;
String[] liste_code;
int etat_actuel = 0;
int repetition = 3;

void setup() {
  //On connecte au démarrage l'arduino avec l'ordinateur (On part du principe que l'arduino sera sur le dernière port série connecté)
  Serial_arduino = new Serial(this, Serial.list()[Serial.list().length-1], 9600);
  
  cp5 = new ControlP5(this); //Gestionnaire d'interface ControlP5
  
  //Creation de la fenètre
  size(largeur_fenetre, hauteur_fenetre);
  background(color(100,100,100));
  
  //Creation de la police d'écriture par défaut
  creer_police();

  //Titre
  creer_texte("Commandes LED DIMMER", position_hauteur);
  creer_ligne(30);

  //Bouton ON/OFF
  cp5.addBang("ON/OFF")
     .setPosition(aligner_au_centre_P5-50,position_hauteur * 1.7)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(50,50)
     ;
     
  cp5.addBang("DOWN")
     .setPosition(aligner_au_centre_P5+50,position_hauteur * 1.7)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(50,50)
     ;
     
  cp5.addBang("UP")
     .setPosition(aligner_au_centre_P5+100,position_hauteur * 1.7)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(50,50)
     ;
     
   champ_repetition =  cp5.addTextfield("REPETITION")
      .setPosition(aligner_au_centre_P5+ 10,position_hauteur * 6.6)
      .setSize(40,40)
      .setFont(police)
      .setAutoClear(false)
      .setText("3")
      ;
   
   cp5.addBang("CONFIRMER")
     .setPosition(aligner_au_centre_P5+60,position_hauteur * 6.6)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(40,40)
     ;  
     
   text("Connecté à " + Serial.list()[Serial.list().length-1],aligner_au_centre, position_hauteur * 10.1);
     
   message("");
}

void draw()
{
   while (Serial_arduino.available() > 0) {
    delay(10);
    String inBuffer = Serial_arduino.readString();   
    if (inBuffer != null) {
      
      switch(etat_actuel) {
      case 1:     
      message("ENVOI ON/OFF: " + inBuffer);
      break;
      case 2:
      message("ENVOI UP: " + inBuffer);
      break;
      case 3:
      message("ENVOI DOWN: " + inBuffer);
      break;
      case 4:
      message("Changement à " + champ_repetition.getText() + " Répétitions: " + inBuffer);
      }
  }
   }
}

//Gestion des boutons
void controlEvent(ControlEvent theEvent)
{

  if (theEvent.getController().getName().equals("ON/OFF"))
  {
    Serial_arduino.write("onoff");
    etat_actuel = 1;
  }
  if (theEvent.getController().getName().equals("UP"))
  {
    Serial_arduino.write("up");
    etat_actuel = 2;
  }
  if (theEvent.getController().getName().equals("DOWN"))
  {
    Serial_arduino.write("down");
    etat_actuel = 3;
  }
  
  if (theEvent.getController().getName().equals("CONFIRMER"))
  {
    Serial_arduino.write("rep");
    message("Modification des répétitions en cours");
    delay(500);
    Serial_arduino.write(champ_repetition.getText());
    etat_actuel = 4;
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
void message(String texte)
{
effacer_message();
fill(255,0,0);
text(texte,aligner_au_centre,position_hauteur * 6);
}

//Efface (créer la boite en faite) le message d'erreur
void effacer_message()
{
fill(255);
rect(0,position_hauteur * 5, largeur_fenetre-1, 30); 
}

