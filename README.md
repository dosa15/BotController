# BotController
Controller for Technocrats Task Bot

The controller of the Bot has 14 different buttons, and one joystick. 

Functions of various buttons:
Forward - to move the bot in the forward direction. 
Backward - to move the bot in the backward direction. 
Front Up - raises the front wheels up using actuator. 
Front Down - lowers the front wheels down using actuator. 
Middle Up - raises the middle wheels up using Johnson motor. 
Middle Down - lowers the middle wheels down using Johnson motor. 
Back Up - raises the rear wheels up using Johnson motor. 
Back Down - lowers the rear wheels down using Johnson motor. 
Gripper Toggle - is a toggle type of button that can be used to either activate or deactivate the grippers to hold/release the weight. 
Auto/Manual Toggle - is a toggle type of button that can be used to change the bot’s control type to either autonomous or manual modes.

The bot is connected to the RC application through a Bluetooth communication medium. The connection process including pairing and connecting to the HC-05 module is done within the app itself: 
The BT button is used to turn on the Bluetooth feature of the mobile. 
The Discoverable button is used to make the mobile app discoverable to devices in the outside world. 
The Discover button is used to list the available unpaired devices in the List View located at the middle of the screen. The required device can be paired with by tapping its name in the List View
The Connect button is used to connect to the device that has been paired with the mobile. 

The joystick’s movement controls the direction that the bot is facing. Output is given based on the angle that the joystick makes with respect to its coordinate plane and the direction it is dragged to, following which the bot will turn in the same direction as the joystick, up to the angle that it receives from the same.
