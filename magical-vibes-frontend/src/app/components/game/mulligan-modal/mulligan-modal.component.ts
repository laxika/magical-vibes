import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { Card, MulliganResolvedNotification, SelectCardsToBottomNotification } from '../../../services/websocket.service';
import { CardDisplayComponent } from '../card-display/card-display.component';

@Component({
  selector: 'app-mulligan-modal',
  standalone: true,
  imports: [CardDisplayComponent],
  templateUrl: './mulligan-modal.component.html',
  styleUrl: './mulligan-modal.component.css'
})
export class MulliganModalComponent {
  @Input() hand: Card[] = [];
  @Input() player1Name = '';
  @Input() player2Name = '';
  @Input() player1HandSize = 0;
  @Input() player2HandSize = 0;
  @Input() player1DeckSize = 0;
  @Input() player2DeckSize = 0;
  @Input() mulliganCount = 0;
  @Input() canMulligan = true;

  @Output() keepHandEvent = new EventEmitter<void>();
  @Output() takeMulliganEvent = new EventEmitter<void>();
  @Output() confirmBottomCardsEvent = new EventEmitter<number[]>();
  @Output() cardHover = new EventEmitter<Card>();
  @Output() cardHoverEnd = new EventEmitter<void>();

  opponentKept = signal(false);
  selfKept = signal(false);
  selectingBottomCards = signal(false);
  bottomCardCount = signal(0);
  selectedCardIndices = signal(new Set<number>());

  get canConfirmBottom(): boolean {
    return this.selectedCardIndices().size === this.bottomCardCount();
  }

  keepHand(): void {
    if (!this.selfKept()) {
      this.keepHandEvent.emit();
    }
  }

  takeMulligan(): void {
    if (!this.selfKept()) {
      this.takeMulliganEvent.emit();
    }
  }

  toggleCardSelection(index: number): void {
    const current = this.selectedCardIndices();
    if (current.has(index)) {
      const updated = new Set(current);
      updated.delete(index);
      this.selectedCardIndices.set(updated);
    } else if (current.size < this.bottomCardCount()) {
      const updated = new Set(current);
      updated.add(index);
      this.selectedCardIndices.set(updated);
    }
  }

  isCardSelected(index: number): boolean {
    return this.selectedCardIndices().has(index);
  }

  confirmBottomCards(): void {
    if (this.canConfirmBottom) {
      this.confirmBottomCardsEvent.emit(Array.from(this.selectedCardIndices()));
      this.selectingBottomCards.set(false);
      this.selectedCardIndices.set(new Set());
    }
  }

  // Called by parent via @ViewChild
  handleMulliganResolved(resolved: MulliganResolvedNotification, myName: string): void {
    if (resolved.playerName === myName) {
      if (resolved.kept) {
        this.selfKept.set(true);
      }
    } else {
      this.opponentKept.set(resolved.kept);
    }
  }

  handleSelectCardsToBottom(msg: SelectCardsToBottomNotification): void {
    this.selectingBottomCards.set(true);
    this.bottomCardCount.set(msg.count);
    this.selectedCardIndices.set(new Set());
  }

  resetState(): void {
    this.opponentKept.set(false);
    this.selfKept.set(false);
    this.selectingBottomCards.set(false);
    this.bottomCardCount.set(0);
    this.selectedCardIndices.set(new Set());
  }
}
