import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Card, Permanent } from '../../../services/websocket.service';
import { CardDisplayComponent } from '../card-display/card-display.component';
import { CombatGroup, CombatBlocker, AttachedAura } from '../battlefield.utils';

@Component({
  selector: 'app-combat-zone',
  standalone: true,
  imports: [CardDisplayComponent],
  templateUrl: './combat-zone.component.html',
  styleUrl: './combat-zone.component.css'
})
export class CombatZoneComponent {
  @Input() combatPairings: CombatGroup[] = [];
  @Input() declaringAttackers = false;
  @Input() declaringBlockers = false;
  @Input() selectedBlockerIndex: number | null = null;
  @Input() stackTargetId: string | null = null;

  // Targeting state from choice service (read-only display flags)
  @Input() isSelectingTarget = false;
  @Input() isDistributingDamage = false;

  // Functions passed from parent for evaluating state
  @Input() isValidTarget!: (perm: Permanent) => boolean;
  @Input() isSelectedAttacker!: (index: number) => boolean;
  @Input() getAttachedAuras!: (permanentId: string) => AttachedAura[];
  @Input() getDamageAssigned!: (permanentId: string) => number;
  @Input() unassignDamage!: (permanentId: string) => void;

  @Output() attackerClick = new EventEmitter<CombatGroup>();
  @Output() blockerClick = new EventEmitter<CombatBlocker>();
  @Output() auraClick = new EventEmitter<AttachedAura>();
  @Output() cardHover = new EventEmitter<{ card: Card; permanent?: Permanent }>();
  @Output() cardHoverEnd = new EventEmitter<void>();

  onCardHover(card: Card, permanent?: Permanent): void {
    this.cardHover.emit({ card, permanent });
  }

  onCardHoverEnd(): void {
    this.cardHoverEnd.emit();
  }
}
