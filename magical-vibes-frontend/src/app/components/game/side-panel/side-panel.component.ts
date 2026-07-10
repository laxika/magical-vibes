import { Component, Input, Output, EventEmitter, signal, inject, HostListener, ViewChild, ElementRef, OnChanges, SimpleChanges, AfterViewChecked } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Card, ModifierLine, Permanent, StackEntry } from '../../../services/websocket.service';
import { GameChoiceService } from '../../../services/game-choice.service';
import { ManaSymbolService } from '../../../services/mana-symbol.service';
import { CardDisplayComponent } from '../card-display/card-display.component';
import { formatKeywords } from '../../../utils/format-utils';

interface BreakdownRow {
  label: string;
  value: string;
  tone: 'positive' | 'negative' | 'neutral';
}

interface ModifierBreakdown {
  /** Whether to show the Base/Current P/T frame around the rows. */
  showPt: boolean;
  basePower: number;
  baseToughness: number;
  finalPower: number;
  finalToughness: number;
  rows: BreakdownRow[];
}

@Component({
  selector: 'app-side-panel',
  standalone: true,
  imports: [CardDisplayComponent],
  templateUrl: './side-panel.component.html',
  styleUrl: './side-panel.component.css'
})
export class SidePanelComponent implements OnChanges, AfterViewChecked {
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
  @Input() manaEntries: { color: string; count: number }[] = [];
  @Input() declaringAttackers = false;
  @Input() declaringBlockers = false;
  @Input() attackTaxPerCreature = 0;
  @Input() mustAttackWithAtLeastOne = false;
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
  @Input() isGraveyardAbilityActivatable!: (index: number) => boolean;
  @Input() isFlashbackPlayable!: (index: number) => boolean;
  @Input() getPlayerName!: (playerId: string) => string;
  @Input() getStackEntryTargetName!: (entry: StackEntry) => string | null;
  @Input() searchTaxCost = 0;
  @Input() myPlayerIndex = 0;
  @Input() isMindControlling = false;
  @Input() mindControlledPlayerName = '';

  @Output() passPriority = new EventEmitter<void>();
  @Output() paySearchTax = new EventEmitter<void>();
  @Output() confirmAttackers = new EventEmitter<void>();
  @Output() confirmBlockers = new EventEmitter<void>();
  @Output() playerBadgeClick = new EventEmitter<number>();
  @Output() surrenderClick = new EventEmitter<void>();
  @Output() cardHover = new EventEmitter<{ card: Card; permanent?: Permanent | null }>();
  @Output() cardHoverEnd = new EventEmitter<void>();
  @Output() stackEntryHover = new EventEmitter<StackEntry>();
  @Output() stackEntryHoverEnd = new EventEmitter<void>();
  @Output() graveyardLandPlay = new EventEmitter<number>();
  @Output() graveyardAbilityActivate = new EventEmitter<number>();
  @Output() flashbackPlay = new EventEmitter<number>();

  activeTab = signal<'log' | 'stack' | 'graveyard'>('log');
  showPlayerMenu = signal(false);
  logUnreadCount = signal(0);

  @ViewChild('logEntries') private logEntriesRef?: ElementRef<HTMLElement>;
  private logPinnedToBottom = true;
  private shouldScrollLog = false;
  private seenLogCount = 0;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['gameLog']) {
      if (this.activeTab() === 'log') {
        this.seenLogCount = this.gameLog.length;
        if (this.logPinnedToBottom) {
          this.shouldScrollLog = true;
        }
      } else {
        this.logUnreadCount.set(this.gameLog.length - this.seenLogCount);
      }
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollLog && this.logEntriesRef) {
      const el = this.logEntriesRef.nativeElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScrollLog = false;
    }
  }

  openLogTab(): void {
    this.activeTab.set('log');
    this.seenLogCount = this.gameLog.length;
    this.logUnreadCount.set(0);
    this.logPinnedToBottom = true;
    this.shouldScrollLog = true;
  }

  onLogScroll(): void {
    const el = this.logEntriesRef?.nativeElement;
    if (el) {
      this.logPinnedToBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 24;
    }
  }

  // Opponent is always shown on the left, my player on the right
  get opponentPlayerIndex(): number { return 1 - this.myPlayerIndex; }

  get opponentBadgeIsActive(): boolean { return this.opponentPlayerIndex === 0 ? this.isActivePlayer0 : this.isActivePlayer1; }
  get opponentBadgeHoldsPriority(): boolean { return this.opponentPlayerIndex === 0 ? this.holdsPriority0 : this.holdsPriority1; }
  get opponentBadgeLifeTotal(): number { return this.opponentPlayerIndex === 0 ? this.lifeTotal0 : this.lifeTotal1; }
  get opponentBadgePoisonCounters(): number { return this.opponentPlayerIndex === 0 ? this.poisonCounters0 : this.poisonCounters1; }
  get opponentBadgePlayerId(): string { return this.opponentPlayerIndex === 0 ? this.playerId0 : this.playerId1; }
  get opponentBadgeHandSize(): number { return this.opponentPlayerIndex === 0 ? this.player1HandSize : this.player2HandSize; }
  get opponentBadgeDeckSize(): number { return this.opponentPlayerIndex === 0 ? this.player1DeckSize : this.player2DeckSize; }

  get myBadgeName(): string { return this.myPlayerIndex === 0 ? this.player1Name : this.player2Name; }
  get myBadgeIsActive(): boolean { return this.myPlayerIndex === 0 ? this.isActivePlayer0 : this.isActivePlayer1; }
  get myBadgeHoldsPriority(): boolean { return this.myPlayerIndex === 0 ? this.holdsPriority0 : this.holdsPriority1; }
  get myBadgeLifeTotal(): number { return this.myPlayerIndex === 0 ? this.lifeTotal0 : this.lifeTotal1; }
  get myBadgePoisonCounters(): number { return this.myPlayerIndex === 0 ? this.poisonCounters0 : this.poisonCounters1; }
  get myBadgePlayerId(): string { return this.myPlayerIndex === 0 ? this.playerId0 : this.playerId1; }
  get myBadgeHandSize(): number { return this.myPlayerIndex === 0 ? this.player1HandSize : this.player2HandSize; }
  get myBadgeDeckSize(): number { return this.myPlayerIndex === 0 ? this.player1DeckSize : this.player2DeckSize; }

  switchToStackTab(): void {
    this.activeTab.set('stack');
  }

  switchToLogTabIfOnStack(): void {
    if (this.activeTab() === 'stack') {
      this.openLogTab();
    }
  }

  isStackTargetPlayer(playerIndex: number): boolean {
    const playerId = playerIndex === 0 ? this.playerId0 : this.playerId1;
    return this.stackTargetId === playerId;
  }

  isStackTargetSpell(entry: StackEntry): boolean {
    return this.stackTargetId === entry.cardId;
  }

  readonly manaColors = ['W', 'U', 'B', 'R', 'G', 'C'];

  manaCount(color: string): number {
    return this.manaEntries.find(e => e.color === color)?.count ?? 0;
  }

  manaSymbol(color: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(
      this.manaSymbolService.replaceSymbols(`{${color}}`)
    );
  }

  formatAbilityDescription(description: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(
      this.manaSymbolService.replaceSymbols(description)
    );
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.showPlayerMenu.set(false);
  }

  onPlayerBadgeClick(event: MouseEvent, playerIndex: number): void {
    const isTargeting = this.choice.targeting.selectingTarget || this.choice.targeting.multiTargeting || this.choice.choosingPermanent;
    if (playerIndex === this.myPlayerIndex && !isTargeting) {
      event.stopPropagation();
      this.showPlayerMenu.update(v => !v);
    } else {
      this.showPlayerMenu.set(false);
      this.playerBadgeClick.emit(playerIndex);
    }
  }

  onSurrenderClick(): void {
    this.showPlayerMenu.set(false);
    this.surrenderClick.emit();
  }

  // ========== Hovered-permanent modifier breakdown ==========
  // Per-source attribution from permanent.modifierLines (layer 6/7 provenance computed by the
  // engine), reconciled against the layered wire aggregates so the rows always sum exactly to
  // the displayed P/T: base lines fold last-wins per component over the printed base, counters
  // come from the counters map, and anything un-attributed (one-shot pumps stored on the
  // permanent) lands in an "Other effects" remainder row.

  get modifierBreakdown(): ModifierBreakdown | null {
    const p = this.hoveredPermanent;
    if (!p) return null;
    const lines = p.modifierLines ?? [];
    const creatureLike = p.card.power != null || p.animatedCreature;

    const plusCounters = p.counters['PLUS_ONE_PLUS_ONE'] ?? 0;
    const minusCounters = p.counters['MINUS_ONE_MINUS_ONE'] ?? 0;
    const counterDelta = plusCounters - minusCounters;

    const baseLines = lines.filter(l => l.basePower != null || l.baseToughness != null);
    const switchLines = lines.filter(l => l.switchesPt);
    // Two switches cancel — the displayed effective P/T is swapped when the parity is odd,
    // so un-swap it to reconcile the additive rows.
    const switched = switchLines.length % 2 === 1;
    const preSwitchPower = switched ? p.effectiveToughness : p.effectivePower;
    const preSwitchToughness = switched ? p.effectivePower : p.effectiveToughness;

    let basePower: number;
    let baseToughness: number;
    if (baseLines.length > 0) {
      basePower = p.card.power ?? 0;
      baseToughness = p.card.toughness ?? 0;
      for (const l of baseLines) {
        if (l.basePower != null) basePower = l.basePower;
        if (l.baseToughness != null) baseToughness = l.baseToughness;
      }
    } else {
      // No base-setting effects: reconstruct the printed base from the aggregates
      // (also covers legacy paths the attribution doesn't know about).
      basePower = p.effectivePower - p.powerModifier - counterDelta;
      baseToughness = p.effectiveToughness - p.toughnessModifier - counterDelta;
    }

    const rows: BreakdownRow[] = [];
    let ptTouched = baseLines.length > 0 || switchLines.length > 0;

    for (const l of baseLines) {
      rows.push({ label: l.source, value: this.lineValue(l), tone: this.lineTone(l) });
    }
    if (plusCounters > 0) {
      rows.push({ label: `+1/+1 counters (${plusCounters})`, value: `+${plusCounters}/+${plusCounters}`, tone: 'positive' });
      ptTouched = true;
    }
    if (minusCounters > 0) {
      rows.push({ label: `−1/−1 counters (${minusCounters})`, value: `−${minusCounters}/−${minusCounters}`, tone: 'negative' });
      ptTouched = true;
    }

    let attributedPower = 0;
    let attributedToughness = 0;
    const attributedGained = new Set<string>();
    const attributedRemoved = new Set<string>();
    let anyLosesAll = false;
    for (const l of lines) {
      attributedPower += l.power;
      attributedToughness += l.toughness;
      l.gainedKeywords.forEach(k => attributedGained.add(k));
      l.removedKeywords.forEach(k => attributedRemoved.add(k));
      anyLosesAll = anyLosesAll || l.losesAllAbilities;
      if (baseLines.includes(l) || switchLines.includes(l)) continue;
      rows.push({ label: l.source, value: this.lineValue(l), tone: this.lineTone(l) });
      if (l.power !== 0 || l.toughness !== 0) ptTouched = true;
    }

    // Un-attributed remainder: keeps the rows summing exactly to the displayed P/T.
    const remainderPower = creatureLike ? preSwitchPower - basePower - counterDelta - attributedPower : 0;
    const remainderToughness = creatureLike ? preSwitchToughness - baseToughness - counterDelta - attributedToughness : 0;
    const printed = p.card.keywords;
    const otherGained = p.grantedKeywords.filter(k => !printed.includes(k) && !attributedGained.has(k));
    const otherLost = anyLosesAll ? []
        : p.removedKeywords.filter(k => printed.includes(k) && !attributedRemoved.has(k));
    if (remainderPower !== 0 || remainderToughness !== 0 || otherGained.length > 0 || otherLost.length > 0) {
      const parts: string[] = [];
      if (remainderPower !== 0 || remainderToughness !== 0) {
        parts.push(`${this.signed(remainderPower)}/${this.signed(remainderToughness)}`);
        ptTouched = true;
      }
      if (otherGained.length > 0) parts.push(formatKeywords(otherGained));
      if (otherLost.length > 0) parts.push('loses ' + formatKeywords(otherLost));
      const positive = remainderPower > 0 || remainderToughness > 0 || otherGained.length > 0;
      const negative = remainderPower < 0 || remainderToughness < 0 || otherLost.length > 0;
      rows.push({ label: 'Other effects', value: parts.join(', '), tone: positive && !negative ? 'positive' : negative && !positive ? 'negative' : 'neutral' });
    }

    for (const l of switchLines) {
      rows.push({ label: l.source, value: 'switches P/T', tone: 'neutral' });
    }

    if (rows.length === 0) return null;
    return {
      showPt: creatureLike && ptTouched,
      basePower, baseToughness,
      finalPower: p.effectivePower,
      finalToughness: p.effectiveToughness,
      rows,
    };
  }

  private lineValue(l: ModifierLine): string {
    const parts: string[] = [];
    if (l.power !== 0 || l.toughness !== 0) {
      parts.push(`${this.signed(l.power)}/${this.signed(l.toughness)}`);
    }
    if (l.basePower != null || l.baseToughness != null) {
      parts.push(`base ${l.basePower ?? '—'}/${l.baseToughness ?? '—'}`);
    }
    if (l.gainedKeywords.length > 0) {
      parts.push(formatKeywords(l.gainedKeywords));
    }
    if (l.losesAllAbilities) {
      parts.push('loses all abilities');
    } else if (l.removedKeywords.length > 0) {
      parts.push('loses ' + formatKeywords(l.removedKeywords));
    }
    return parts.join(', ');
  }

  private lineTone(l: ModifierLine): 'positive' | 'negative' | 'neutral' {
    const positive = l.power > 0 || l.toughness > 0 || l.gainedKeywords.length > 0;
    const negative = l.power < 0 || l.toughness < 0 || l.removedKeywords.length > 0 || l.losesAllAbilities;
    if (positive && !negative) return 'positive';
    if (negative && !positive) return 'negative';
    return 'neutral';
  }

  signed(n: number): string {
    return n < 0 ? `−${-n}` : `+${n}`;
  }

  onCardHover(card: Card, permanent?: Permanent | null): void {
    this.cardHover.emit({ card, permanent });
  }

  onCardHoverEnd(): void {
    this.cardHoverEnd.emit();
  }
}
