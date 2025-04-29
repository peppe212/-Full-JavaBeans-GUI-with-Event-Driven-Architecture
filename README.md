# 🂠 Matching Pairs Game – A Full JavaBeans GUI with Event-Driven Architecture

A complete single-player memory game built with Java Swing, designed to demonstrate JavaBeans, bound and constrained properties, and the Observer (Publish-Subscribe) pattern using custom events and listeners.

This project was developed for an academic assignment focused on advanced JavaBeans design and GUI programming, and shows how to decouple components in a clean and reusable way.

---

## 🎮 Game Overview

The game displays a board of 8 hidden cards. The player clicks to flip cards face up, attempting to find pairs with matching values.  
When all pairs are found, the game ends. A Shuffle button resets the game; an Exit button closes it.

---

## 💡 Features

- Fully modular JavaBeans structure
- Uses PropertyChangeSupport and VetoableChangeSupport
- Custom event classes: ShuffleEvent, MatchedEvent
- Custom listeners: ShuffleListener, MatchedListener
- Decoupled communication between components
- Visual GUI with JButton, JLabel, and event-driven logic
- Scripts for building JavaBeans .jar files with proper manifests

---

## 🧠 JavaBeans Components

| Bean         | Description |
|--------------|-------------|
| Board        | The main game window (JFrame). Publishes ShuffleEvent |
| Card         | JButton with constrained and bound 'state'. Listens to shuffle and match events |
| Controller   | Game logic. Vetoes invalid state changes. Publishes MatchedEvent |
| Counter      | Tracks the number of card flips. Listens to Card state |
| Challenge    | (Optional) Tracks best score. Listens to Controller |

Other custom classes:
- MatchedEvent, MatchedListener
- ShuffleEvent, ShuffleListener

---

## 🔄 Observer Design Pattern

| Publisher    | Event Type        | Subscribers                      |
|--------------|-------------------|----------------------------------|
| Board        | ShuffleEvent      | Card, Controller, Counter        |
| Card         | PropertyChange    | Controller, Counter              |
| Card         | VetoableChange    | Controller                       |
| Controller   | MatchedEvent      | Card                             |
| Controller   | PropertyChange    | Challenge                        |

---

## 🧪 Build Instructions

### Folder Structure

Matching_Pairs_Game/  
├── src/                         → All .java source files  
├── beans_jars/                  → Compiled .jar for each bean with manifest  
├── windows_script_jar_beans.bat → Windows build script  
├── unix_script_jar_beans.sh     → Linux/macOS build script  
├── README.md                    → This file  

### Build JARs

**On Windows**  
Double-click `windows_script_jar_beans.bat`

**On Linux/macOS**  
Run: chmod +x unix_script_jar_beans.sh
./unix_script_jar_beans.sh

Each `.jar` includes:  
- The `.class` of the JavaBean  
- Inner classes (e.g. Card$1.class)  
- A manifest file with:  
  Manifest-Version: 1.0  
  Java-Bean: True

---

## 🎓 Educational Objectives

This project was developed as part of the **Advanced Programming** Master's Degree course in Computer Science and is aligned with its learning goals:

- Implementing **JavaBeans** with correct bound and constrained properties  
- Designing event-driven architectures using custom listeners and standard Java patterns  
- Applying the **Observer pattern** (Publish/Subscribe) for inter-component communication  
- Building reusable GUI components with Swing  
- Encapsulating behavior and avoiding tight coupling between beans  
- Managing game state transitions cleanly through vetoable logic

---

## 👤 Author

**Giuseppe Muschetta**  
Bachelor's Degree in Computer Science  
Master's Degree (in progress) in Data Science and Business Informatics  
University of Pisa  
📍 Pisa, Italy  
📧 peppe212@gmail.com  

---

## 📜 License

This project is intended for academic and educational use only.

