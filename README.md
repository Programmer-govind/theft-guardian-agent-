# 🔐 Theft Guardian Agent

*Autonomous Mobile Security Agent for Early Theft Detection*

---

## 📌 Overview

Theft Guardian Agent is a mobile-native autonomous Android agent designed to detect potential phone theft scenarios and respond instantly without requiring user interaction. The agent focuses on speed, offline reliability, and explainable decision-making to act before a stolen device is switched off.

---

## ❗ Problem Statement

Smartphone theft often happens within seconds, leaving the user no time to react. Most existing solutions rely on manual actions, internet connectivity, or post-theft tracking, which fail in real-world snatching scenarios. There is a need for an autonomous mobile agent that can independently detect suspicious behavior and trigger emergency actions immediately.

---

## 💡 Solution Summary

Theft Guardian Agent continuously observes contextual signals on the device such as unlock attempts, authentication failures, sudden motion, rapid screen activity, and power-off attempts. Each signal contributes to a confidence score. When the score crosses a defined threshold, the agent instantly enters Theft Mode and executes emergency actions without user confirmation.

---

## 🧠 Agent Architecture

### Agent States

* **NORMAL** – No suspicious activity
* **SUSPICIOUS** – Multiple correlated signals detected
* **THEFT MODE** – Confidence threshold crossed, emergency actions triggered

### Key Signals Used

* Face unlock attempt / failure (simulated)
* Wrong PIN attempts
* Sudden high acceleration (snatch pattern)
* Rapid screen ON/OFF
* Power button long press
* Sudden location change

---

## 📊 Confidence Scoring Logic

Each signal contributes a predefined score. The agent evaluates events occurring within a short time window.

Example:

* Face unlock attempt: +30
* Face unlock failure: +40
* Wrong PIN attempt: +20
* Sudden jerk detected: +25
* Power button long press: +30

**Theft Mode Trigger:**
Score ≥ 70 within a short time window.

This rule-based approach ensures explainability and avoids black-box decision-making.

---

## ⚡ Emergency Actions (Theft Mode)

* Sends emergency SMS alerts to trusted contacts (offline-capable)
* Initiates emergency call to **112**
* Preserves last known location and timestamp
* Activates Theft Mode without user confirmation

---

## 📱 Demo Approach

Due to Android OS security restrictions and current DroidRun agent limitations, certain low-level system events are demonstrated using controlled simulations. The agent logic, confidence scoring, and emergency automation flows remain identical to real-world execution.

---

## ⚠️ Limitations

* Capturing photos during face unlock on a locked screen is restricted by Android OS security policies.
* Some hardware-level events are simulated for demonstration purposes.
* The project focuses on agent behavior and decision logic rather than OS-level exploitation.

---

## 🚀 Future Scope

* Deeper OS-level integration with privileged permissions
* Cloud-assisted recovery workflows
* Cross-device emergency coordination
* Law enforcement integration APIs

---

## 🛠 Tech Stack

* Android (Kotlin)
* DroidRun Agent Framework
* Android Sensors & Telephony APIs
* Rule-based Decision Engine

---

## 🔗 Project Links

* **Mobile Agent Repo**: [Guardian Agent Mobile](https://github.com/Programmer-govind/theft-guardian-agent-)
* **Backend Repo**: [Guardian Agent Backend](https://github.com/Abhinav-676/guardian-agent-backend)

---

## 📽 Demo Video

YouTube Link: *(To be added)*

---

## 📄 License

MIT License
