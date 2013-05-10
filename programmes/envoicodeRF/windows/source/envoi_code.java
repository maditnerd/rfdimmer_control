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

public class envoi_code extends PApplet {

/*
 R\u00e9cup\u00e9rateur de codes 433.92Mhz
 Auteur: Sarrailh R\u00e9mi (maditnerd)
 License : Gplv3
 http://www.tldrlegal.com/l/GPL3
 
 Description: 
 Ce code permet d'envoyer des codes \u00e0 plusieurs LED DIMMER (ou n'importe quoi fonctionnant en 433.92Mhz).
 
 Arduino:
 Ce code n\u00e9cessite de programmer l'Arduino avec ce code (\u00e0 mettre)
 et d'y brancher un r\u00e9cepteur 433.92Mhz sur la pin (broche) 2
 
 NECESSITE LA BIBLIOTHEQUE controlP5 (\u00e0 mettre dans librairies dans votre sketchbook)
 http://www.sojamo.de/libraries/controlP5/#installation

 */

 //Biblioth\u00e8que de communication en s\u00e9rie
 // Biblioth\u00e8que pour l'interface

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

PFont police; //Police d'\u00e9criture
Serial Serial_arduino;  // Connexion au port s\u00e9rie
int nbcode = 0;
int variable = 20;
String[] liste_code;
int etat_actuel = 0;
int repetition = 3;

public void setup() {
  //On connecte au d\u00e9marrage l'arduino avec l'ordinateur (On part du principe que l'arduino sera sur le derni\u00e8re port s\u00e9rie connect\u00e9)
  Serial_arduino = new Serial(this, Serial.list()[Serial.list().length-1], 9600);
  
  cp5 = new ControlP5(this); //Gestionnaire d'interface ControlP5
  
  //Creation de la fen\u00e8tre
  size(largeur_fenetre, hauteur_fenetre);
  background(color(100,100,100));
  
  //Creation de la police d'\u00e9criture par d\u00e9faut
  creer_police();

  //Titre
  creer_texte("Commandes LED DIMMER", position_hauteur);
  creer_ligne(30);

  //Bouton ON/OFF
  cp5.addBang("ON/OFF")
     .setPosition(aligner_au_centre_P5-50,position_hauteur * 1.7f)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(50,50)
     ;
     
  cp5.addBang("DOWN")
     .setPosition(aligner_au_centre_P5+50,position_hauteur * 1.7f)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(50,50)
     ;
     
  cp5.addBang("UP")
     .setPosition(aligner_au_centre_P5+100,position_hauteur * 1.7f)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(50,50)
     ;
     
   champ_repetition =  cp5.addTextfield("REPETITION")
      .setPosition(aligner_au_centre_P5+ 10,position_hauteur * 6.6f)
      .setSize(40,40)
      .setFont(police)
      .setAutoClear(false)
      .setText("3")
      ;
   
   cp5.addBang("CONFIRMER")
     .setPosition(aligner_au_centre_P5+60,position_hauteur * 6.6f)
     .setTriggerEvent(Bang.RELEASE)
     .setSize(40,40)
     ;  
     
   text("Connect\u00e9 \u00e0 " + Serial.list()[Serial.list().length-1],aligner_au_centre, position_hauteur * 10.1f);
     
   message("");
}

public void draw()
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
      message("Changement \u00e0 " + champ_repetition.getText() + " R\u00e9p\u00e9titions: " + inBuffer);
      }
  }
   }
}

//Gestion des boutons
public void controlEvent(ControlEvent theEvent)
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
    message("Modification des r\u00e9p\u00e9titions en cours");
    delay(500);
    Serial_arduino.write(champ_repetition.getText());
    etat_actuel = 4;
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
public void message(String texte)
{
effacer_message();
fill(255,0,0);
text(texte,aligner_au_centre,position_hauteur * 6);
}

//Efface (cr\u00e9er la boite en faite) le message d'erreur
public void effacer_message()
{
fill(255);
rect(0,position_hauteur * 5, largeur_fenetre-1, 30); 
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "envoi_code" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
