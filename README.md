# Patam2Proj
## Flight Gear Simulator, Anomaly Detector and Player


In this project We’ve created a GUI application for the Flight Gear Simulator,
We have transmitted Data of a Training flight and Data of the flight with Anomalies In comparison to the Train Flight And we are using it as a Test data.

In order to create the GUI application we've been using MVVM architecture.
Via the Player application we can control the flight speed and the way we display the flight itself.

In order to detect Anomalies in the test flight we can use one of the three algorithms:
Regression line, ZScore and an Hybrid algorithm in which we can select an attribute and the suitable algorithm will be activated.
The regression line for correlation above 0.95 , Welzl algorithm for correlation between 0.5 - 0.95 and for less will be using ZScore algorithm .

In our Data We have a table with attributes and its values according to the timeline.
We are also Displaying the Data of the selected attribute with its most correlated attribute in the graph nearby.



Developed by: Ben Basil, Yuval Grinstein, Ariel Michelle Cowan, Orel Bilibook.
