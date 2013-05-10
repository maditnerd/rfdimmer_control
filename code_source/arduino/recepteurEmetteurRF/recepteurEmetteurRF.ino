/*
Description: Récepteur / Emetteur de code 433.92Mhz
Auteur : Sarrailh Rémi
Licence : Gplv3

Mode Apprentissage qui envoie sur le port
série le code en décimal 

Mode envoi qui envoit l'ensemble des codes définit dans la CONFIGURATION
lorsqu'il reçoit par le port série
onoff (éteint/allume)
up (augmente l'éclairage)
down (diminue l'éclairage)

Emetteur sur Pin 8
Récepteur sur Pin 2 (Interrupt 0)
Switch (ON/OFF) sur pin 5
LED RGB avec anode commune sur Pin 7/6

Nécessite la bibliothèqe RCSwitch !

*/
#include <RCSwitch.h>

//////////////////////////////////////////////////////////
//CONFIGURATION///////////////////////////////////////////

//Tout le programme est paramètrables ici.

//Tableau avec les ID à 4 chiffres
unsigned long code_on_4[] = {4398860, 15760652,4398111,4398112,4398113,4398114,4398115,4398116,4398117,4398118,4398119,4398120,4398121,4398122,4398123,4398124,1398125,4398125,4398124,4398127};
//Tableau avec les ID à 5 chiffres
unsigned long code_on_5[] = {7732945, 7953873,4398111,4398112,4398113,4398114,4398115,4398116,4398117,4398118,4398119,4398120,4398121,4398122,4398123,4398124,1398125,4398125,4398124,4398127};

//Taille des tableaux (nombre de codes dans chaque tableau)
int code_on_4_size = 20;
int code_on_5_size = 20;

//Répétition de nombres de fois où les codes sont envoyées
int repetition = 3;
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////

/*
         Variables
*/

//Objet Transmetteur/Récepteur
RCSwitch mySwitch = RCSwitch();

//Gestion du port série (USB)
long prevvalue = 0;
int correctvalue = 0;
String readString = "";



//Choix du Mode (0 envoi / 1 réception)
int mode = 0;

//Gestion des broches (pins) de l'arduino

//Interrupteur
int bouton = 5;
//LED Rouge/Verte
int led_rouge = 7;
int led_verte =  6;

//rx récepteur (0 => 2) car on parle de l'interrupt pas du pin
//tx transmetteur
int rx = 0;
int tx = 8;

//Code lancé au démarrage
void setup() {
  //Activation de la communication en série
  Serial.begin(9600);
  
  //Activation du bouton et de la LED
  pinMode(bouton,INPUT_PULLUP);
  pinMode(led_rouge,OUTPUT);
  pinMode(led_verte,OUTPUT);
  
  //On met la LED en HIGH car elle a une anode commune (+ et pas un -)
  //Cela veut dire que en HIGH les leds sont éteintes (inversion)
  digitalWrite(led_rouge,HIGH);
  digitalWrite(led_verte,HIGH);
  
  //Si l'interrupteur est sur OFF
   if (digitalRead(bouton) == 0)
  {
    temoinAllumage(led_verte,200,5); //Faire clignoter la led 5 fois à 0,2 secondes)
    mySwitch.enableTransmit(tx); //Active la transmission
    mySwitch.setRepeatTransmit(repetition); //Paramètre le nombre de répétition
    mode = 0; //Le mode actif est 0 (envoi)
  }
  
  //Si l'interrupteur est sur on
  if (digitalRead(bouton) == 1)
  {
    temoinAllumage(led_rouge,200,5); // Faire clignoter la led 5 fois à 0,2 secondes
    mySwitch.enableReceive(rx);  // Active la réception
    mode = 1; //Le mode actif est 1 (réception)
  }
  
}


//La boucle qui sera executé en permanence
void loop() {
  
  /*
  Mode Envoi
  */

if (mode == 0)
{
  
  //Si la communication série se fait..
 while (Serial.available()) {
  lire_serie(); //Lire le texte caractère par caractère
  }
 
 //Si le texte est prêt à être lue
  if (readString.length() > 0) {
    
    //Si le texte reçu est onoff
    if (readString == "onoff")
    { 
      //Envoi le code brute
      envoi_code(0,0);
      envoi_code(1,0);
      Serial.print("ENDED");
    }
    
    //Si le texte reçu est up
    if (readString == "up")
    {
      //Envoi le code avec 180/3 de plus
      envoi_code(0,180);
      envoi_code(1,3);
      Serial.print("ENDED");
    }
    
    //Si le texte reçu est down
    if (readString == "down")
    {
      //Envoi le code avec 36/1 de plus
      envoi_code(0,36);
      envoi_code(1,1);
      Serial.print("ENDED");
    }
    
  //Serial.print(readString); //DEBUG: Voit ce qui a été récupéré
  readString = ""; //Nettoie la mémoire tampon du série
  }
}
  
/*
Mode apprentissage
*/
  
 if (mode == 1)
{ 
   
  //Si un code est reçu
  if (mySwitch.available()) {
   
    //Enregitrer le code
    long value = mySwitch.getReceivedValue();
    Serial.print(value);
    delay(1000);
          
  }
  //Remise à zéro du récepteur
    mySwitch.resetAvailable();
 }
  
}

//Envoi un code d'une télécommande
//add : Chiffre à ajouter au code décimal
//type : 0 (5 chiffres) 1 (4 chiffres)

void envoi_code(int type,int add)
{
  if (type == 0)
 {
     //Parcours le tableau des codes 4 chiffres
     for (int i = 0; i < code_on_4_size; i++)
      {
        Serial.println(i);
        //Envoi le code au transmetteur (avec/sans ajout)
        mySwitch.send(code_on_4[i]+add,24);
      }
 } 
  
  
 if (type == 1)
 { 
   //Parcours le tableau des codes 5 chiffres
  for (int i = 0; i < code_on_5_size; i++)
      {
        Serial.println(i);
        mySwitch.send(code_on_5[i]+add,24);
      }
 }
 
}

//Gestion du clignotement de LED comme témoin d'allumage
//led_select : pin de la led
//delai : Vitesse de clignotement
//nb : Nombre de clignotement
void temoinAllumage(int led_select,int delai,int nb)
{
  int i = 0;
  while(i < nb)
  {
  digitalWrite(led_select,HIGH);
  delay(delai);
  digitalWrite(led_select,LOW);
  delay(delai);
  i++;
  }
}

//Permet de lire sur le port série une chaine de caractère plutôt qu'un caractère à la fois
void lire_serie() {
delay(3);
// Récupère les données
if (Serial.available() > 0) {
char c = Serial.read();
readString += c;
}
 
}

  

