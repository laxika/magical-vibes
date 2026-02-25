import { Component, Input, Output, EventEmitter, signal, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Card, Permanent, StackEntry } from '../../../services/websocket.service';
import { GameChoiceService } from '../../../services/game-choice.service';
import { ManaSymbolService } from '../../../services/mana-symbol.service';
import { CardDisplayComponent } from '../card-display/card-display.component';

@Component({
  selector: 'app-side-panel',
  standalone: true,
  imports: [CardDisplayComponent],
  templateUrl: './side-panel.component.html',
  styleUrl: './side-panel.component.css'
})
export class SidePanelComponent {
  readonly choice = inject(GameChoiceService);
  private manaSymbolService = inject(ManaSymbolService);
  private sanitizer = inject(DomSanitizer);

  @Input() hoveredCard: Card | null = null;
  @Input() hoveredPermanent: Permanent | null = null;
  @Input() player1Name = '';
  @Input() player2Name = '';
  @Input() player1HandSize = 0;
  @Input() player2HandSize = 0;
  @Input() player1DeckSize = 0;
  @Input() player2DeckSize = 0;
  @Input() turnNumber = 0;
  @Input() stackEntries: StackEntry[] = [];
  @Input() isStackEmpty = true;
  @Input() myGraveyard: Card[] = [];
  @Input() opponentGraveyard: Card[] = [];
  @Input() opponentPlayerName = '';
  @Input() gameLog: string[] = [];
  @Input() declaringAttackers = false;
  @Input() declaringBlockers = false;
  @Input() hasPriority = false;

  // Player info inputs
  @Input() isActivePlayer0 = false;
  @Input() isActivePlayer1 = false;
  @Input() holdsPriority0 = false;
  @Input() holdsPriority1 = false;
  @Input() lifeTotal0 = 20;
  @Input() lifeTotal1 = 20;
  @Input() poisonCounters0 = 0;
  @Input() poisonCounters1 = 0;
  @Input() playerId0 = '';
  @Input() playerId1 = '';
  @Input() stackTargetId: string | null = null;
  @Input() isGraveyardLandPlayable!: (index: number) => boolean;
  @Input() getPlayerName!: (playerId: string) => string;
  @Input() getStackEntryTargetName!: (entry: StackEntry) => string | null;

  @Output() passPriority = new EventEmitter<void>();
  @Output() confirmAttackers = new EventEmitter<void>();
  @Output() confirmBlockers = new EventEmitter<void>();
  @Output() playerBadgeClick = new EventEmitter<number>();
  @Output() cardHover = new EventEmitter<{ card: Card; permanent?: Permanent | null }>();
  @Output() cardHoverEnd = new EventEmitter<void>();
  @Output() stackEntryHover = new EventEmitter<StackEntry>();
  @Output() stackEntryHoverEnd = new EventEmitter<void>();
  @Output() graveyardLandPlay = new EventEmitter<number>();

  activeTab = signal<'game' | 'stack' | 'graveyard'>('game');

  switchToStackTab(): void {
    this.activeTab.set('stack');
  }

  switchToGameTabIfOnStack(): void {
    if (this.activeTab() === 'stack') {
      this.activeTab.set('game');
    }
  }

  isStackTargetPlayer(playerIndex: number): boolean {
    const playerId = playerIndex === 0 ? this.playerId0 : this.playerId1;
    return this.stackTargetId === playerId;
  }

  isStackTargetSpell(entry: StackEntry): boolean {
    return this.stackTargetId === entry.cardId;
  }

  formatAbilityDescription(description: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(
      this.manaSymbolService.replaceSymbols(description)
    );
  }

  onCardHover(card: Card, permanent?: Permanent | null): void {
    this.cardHover.emit({ card, permanent });
  }

  onCardHoverEnd(): void {
    this.cardHoverEnd.emit();
  }
}
