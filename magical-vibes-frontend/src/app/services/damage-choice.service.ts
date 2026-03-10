import { Injectable } from '@angular/core';
import {
  WebsocketService, MessageType,
  CombatDamageTargetView, CombatDamageAssignmentNotification
} from './websocket.service';

@Injectable({ providedIn: 'root' })
export class DamageChoiceService {

  constructor(private websocketService: WebsocketService) {}

  // --- Damage distribution state ---
  distributingDamage = false;
  damageDistributionCardIndex = -1;
  damageDistributionCardName = '';
  damageDistributionXValue = 0;
  damageAssignments: Map<string, number> = new Map();

  // --- Combat damage assignment state ---
  assigningCombatDamage = false;
  combatDamageAttackerName = '';
  combatDamageTotalDamage = 0;
  combatDamageTargets: CombatDamageTargetView[] = [];
  combatDamageAssignments: Map<string, number> = new Map();
  combatDamageIsTrample = false;
  combatDamageIsDeathtouch = false;
  combatDamageAttackerIndex = -1;

  reset(): void {
    this.distributingDamage = false;
    this.damageDistributionCardIndex = -1;
    this.damageDistributionCardName = '';
    this.damageDistributionXValue = 0;
    this.damageAssignments = new Map();
    this.assigningCombatDamage = false;
    this.combatDamageAttackerName = '';
    this.combatDamageTotalDamage = 0;
    this.combatDamageTargets = [];
    this.combatDamageAssignments = new Map();
    this.combatDamageIsTrample = false;
    this.combatDamageIsDeathtouch = false;
    this.combatDamageAttackerIndex = -1;
  }

  // ========== Message handlers ==========

  handleCombatDamageAssignment(msg: CombatDamageAssignmentNotification): void {
    this.assigningCombatDamage = true;
    this.combatDamageAttackerName = msg.attackerName;
    this.combatDamageTotalDamage = msg.totalDamage;
    this.combatDamageTargets = msg.validTargets;
    this.combatDamageAssignments = new Map();
    this.combatDamageIsTrample = msg.isTrample;
    this.combatDamageIsDeathtouch = msg.isDeathtouch;
    this.combatDamageAttackerIndex = msg.attackerIndex;
  }

  // ========== Spell damage distribution ==========

  get damageDistributionRemaining(): number {
    let assigned = 0;
    this.damageAssignments.forEach(v => assigned += v);
    return this.damageDistributionXValue - assigned;
  }

  assignDamage(permanentId: string): void {
    if (!this.distributingDamage || this.damageDistributionRemaining <= 0) return;
    const current = this.damageAssignments.get(permanentId) ?? 0;
    this.damageAssignments.set(permanentId, current + 1);
  }

  unassignDamage(permanentId: string): void {
    if (!this.distributingDamage) return;
    const current = this.damageAssignments.get(permanentId) ?? 0;
    if (current <= 1) {
      this.damageAssignments.delete(permanentId);
    } else {
      this.damageAssignments.set(permanentId, current - 1);
    }
  }

  getDamageAssigned(permanentId: string): number {
    return this.damageAssignments.get(permanentId) ?? 0;
  }

  confirmDamageDistribution(): void {
    if (this.damageDistributionRemaining !== 0) return;
    const assignments: Record<string, number> = {};
    this.damageAssignments.forEach((v, k) => assignments[k] = v);
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.damageDistributionCardIndex,
      xValue: this.damageDistributionXValue,
      damageAssignments: assignments
    });
    this.cancelDamageDistribution();
  }

  cancelDamageDistribution(): void {
    this.distributingDamage = false;
    this.damageDistributionCardIndex = -1;
    this.damageDistributionCardName = '';
    this.damageDistributionXValue = 0;
    this.damageAssignments = new Map();
  }

  // ========== Combat damage assignment ==========

  get combatDamageRemaining(): number {
    let assigned = 0;
    this.combatDamageAssignments.forEach(v => assigned += v);
    return this.combatDamageTotalDamage - assigned;
  }

  get isCombatDamageAssignmentValid(): boolean {
    if (this.combatDamageRemaining !== 0) return false;
    if (!this.combatDamageIsTrample) return true;
    // Trample: each blocker must receive at least lethal damage
    for (const target of this.combatDamageTargets) {
      if (target.isPlayer) continue;
      const lethal = this.combatDamageIsDeathtouch
        ? Math.max(0, 1 - target.currentDamage)
        : target.toughness - target.currentDamage;
      const assigned = this.combatDamageAssignments.get(target.id) ?? 0;
      if (assigned < lethal) return false;
    }
    return true;
  }

  assignCombatDamage(targetId: string): void {
    if (!this.assigningCombatDamage || this.combatDamageRemaining <= 0) return;
    const current = this.combatDamageAssignments.get(targetId) ?? 0;
    this.combatDamageAssignments.set(targetId, current + 1);
  }

  unassignCombatDamage(targetId: string): void {
    if (!this.assigningCombatDamage) return;
    const current = this.combatDamageAssignments.get(targetId) ?? 0;
    if (current <= 1) {
      this.combatDamageAssignments.delete(targetId);
    } else {
      this.combatDamageAssignments.set(targetId, current - 1);
    }
  }

  getCombatDamageAssigned(targetId: string): number {
    return this.combatDamageAssignments.get(targetId) ?? 0;
  }

  confirmCombatDamageAssignment(): void {
    if (!this.isCombatDamageAssignmentValid) return;
    const assignments: Record<string, number> = {};
    this.combatDamageAssignments.forEach((v, k) => assignments[k] = v);
    this.websocketService.send({
      type: MessageType.COMBAT_DAMAGE_ASSIGNED,
      attackerIndex: this.combatDamageAttackerIndex,
      damageAssignments: assignments
    });
    this.cancelCombatDamageAssignment();
  }

  cancelCombatDamageAssignment(): void {
    this.assigningCombatDamage = false;
    this.combatDamageAttackerName = '';
    this.combatDamageTotalDamage = 0;
    this.combatDamageTargets = [];
    this.combatDamageAssignments = new Map();
    this.combatDamageIsTrample = false;
    this.combatDamageIsDeathtouch = false;
    this.combatDamageAttackerIndex = -1;
  }
}
