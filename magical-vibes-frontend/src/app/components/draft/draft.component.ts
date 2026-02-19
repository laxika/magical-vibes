import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  WebsocketService, MessageType, Card,
  DraftJoinedNotification, DraftPackUpdateNotification,
  DeckBuildingStateNotification, TournamentUpdateNotification,
  TournamentGameReadyNotification, DraftFinishedNotification,
  TournamentRound, GameNotification, DraftStatus
} from '../../services/websocket.service';
import { CardDisplayComponent } from '../game/card-display/card-display.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-draft',
  standalone: true,
  imports: [CommonModule, CardDisplayComponent],
  templateUrl: './draft.component.html',
  styleUrl: './draft.component.css'
})
export class DraftComponent implements OnInit, OnDestroy {
  readonly DraftStatus = DraftStatus;

  draftStatus = signal<DraftStatus>(DraftStatus.DRAFTING);
  draftName = signal('');
  setCode = signal('');

  // Drafting state
  currentPack = signal<Card[]>([]);
  draftPool = signal<Card[]>([]);
  packNumber = signal(0);
  pickNumber = signal(0);
  waitingForPack = signal(false);

  // Deck building state
  deckCards = signal<Set<number>>(new Set());
  basicLands = signal<Record<string, number>>({Plains: 0, Island: 0, Swamp: 0, Mountain: 0, Forest: 0});
  timerDisplay = signal('10:00');
  deadlineEpochMillis = signal(0);
  deckSubmitted = signal(false);

  // Tournament state
  bracket = signal<TournamentRound[]>([]);
  currentRound = signal(0);
  roundName = signal('');
  tournamentWinner = signal<string | null>(null);
  waitingForGame = signal(false);

  // Card preview
  hoveredCard = signal<Card | null>(null);

  private subscriptions: Subscription[] = [];
  private timerInterval: ReturnType<typeof setInterval> | null = null;
  private pendingMessages: any[] = [];

  constructor(
    private router: Router,
    public websocketService: WebsocketService
  ) {}

  ngOnInit() {
    if (!this.websocketService.isConnected()) {
      this.router.navigate(['/']);
      return;
    }

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        switch (message.type) {
          case MessageType.DRAFT_JOINED: {
            const msg = message as DraftJoinedNotification;
            this.draftName.set(msg.draftName);
            this.setCode.set(msg.setCode);
            this.draftStatus.set(msg.status as DraftStatus);
            this.websocketService.lastDraftJoined = null;
            break;
          }
          case MessageType.DRAFT_PACK_UPDATE: {
            const msg = message as DraftPackUpdateNotification;
            this.draftStatus.set(DraftStatus.DRAFTING);
            this.currentPack.set(msg.pack);
            this.draftPool.set(msg.pool);
            this.packNumber.set(msg.packNumber);
            this.pickNumber.set(msg.pickNumber);
            this.waitingForPack.set(msg.pack.length === 0);
            this.websocketService.lastDraftPackUpdate = null;
            break;
          }
          case MessageType.DECK_BUILDING_STATE: {
            const msg = message as DeckBuildingStateNotification;
            this.draftStatus.set(DraftStatus.DECK_BUILDING);
            this.draftPool.set(msg.pool);
            this.deadlineEpochMillis.set(msg.deadlineEpochMillis);
            if (!msg.alreadySubmitted) {
              this.deckCards.set(new Set());
              this.basicLands.set({Plains: 0, Island: 0, Swamp: 0, Mountain: 0, Forest: 0});
              this.deckSubmitted.set(false);
            } else {
              this.deckSubmitted.set(true);
            }
            this.startTimer();
            this.websocketService.lastDeckBuildingState = null;
            break;
          }
          case MessageType.TOURNAMENT_UPDATE: {
            const msg = message as TournamentUpdateNotification;
            this.draftStatus.set(DraftStatus.TOURNAMENT);
            this.bracket.set(msg.rounds);
            this.currentRound.set(msg.currentRound);
            this.roundName.set(msg.roundName);
            this.waitingForGame.set(false);
            this.websocketService.lastTournamentUpdate = null;
            break;
          }
          case MessageType.TOURNAMENT_GAME_READY: {
            // Game ready notification - GAME_JOINED will follow with full state
            this.websocketService.inDraft = true;
            break;
          }
          case MessageType.DRAFT_FINISHED: {
            const msg = message as DraftFinishedNotification;
            this.draftStatus.set(DraftStatus.FINISHED);
            this.tournamentWinner.set(msg.winnerName);
            this.websocketService.lastDraftFinished = null;
            break;
          }
          case MessageType.GAME_JOINED: {
            const notification = message as GameNotification;
            if (notification.game) {
              this.websocketService.currentGame = notification.game;
              this.router.navigate(['/game']);
            }
            break;
          }
          case MessageType.ERROR: {
            break;
          }
        }
      })
    );

    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        this.router.navigate(['/']);
      })
    );

    // Restore buffered state (e.g., first pack arrived before component mounted, or returning from a tournament game)
    if (this.websocketService.lastDraftJoined) {
      const msg = this.websocketService.lastDraftJoined;
      this.draftName.set(msg.draftName);
      this.setCode.set(msg.setCode);
      this.draftStatus.set(msg.status as DraftStatus);
      this.websocketService.lastDraftJoined = null;
    }
    if (this.websocketService.lastDraftPackUpdate) {
      const msg = this.websocketService.lastDraftPackUpdate;
      this.draftStatus.set(DraftStatus.DRAFTING);
      this.currentPack.set(msg.pack);
      this.draftPool.set(msg.pool);
      this.packNumber.set(msg.packNumber);
      this.pickNumber.set(msg.pickNumber);
      this.waitingForPack.set(msg.pack.length === 0);
      this.websocketService.lastDraftPackUpdate = null;
    }
    if (this.websocketService.lastDeckBuildingState) {
      const msg = this.websocketService.lastDeckBuildingState;
      this.draftStatus.set(DraftStatus.DECK_BUILDING);
      this.draftPool.set(msg.pool);
      this.deadlineEpochMillis.set(msg.deadlineEpochMillis);
      if (!msg.alreadySubmitted) {
        this.deckCards.set(new Set());
        this.basicLands.set({Plains: 0, Island: 0, Swamp: 0, Mountain: 0, Forest: 0});
        this.deckSubmitted.set(false);
      } else {
        this.deckSubmitted.set(true);
      }
      this.startTimer();
      this.websocketService.lastDeckBuildingState = null;
    }
    if (this.websocketService.lastDraftFinished) {
      const msg = this.websocketService.lastDraftFinished;
      this.draftStatus.set(DraftStatus.FINISHED);
      this.tournamentWinner.set(msg.winnerName);
      this.websocketService.lastDraftFinished = null;
    }
    if (this.websocketService.lastTournamentUpdate) {
      const msg = this.websocketService.lastTournamentUpdate;
      if (this.draftStatus() !== DraftStatus.FINISHED) {
        this.draftStatus.set(DraftStatus.TOURNAMENT);
      }
      this.bracket.set(msg.rounds);
      this.currentRound.set(msg.currentRound);
      this.roundName.set(msg.roundName);
      this.websocketService.lastTournamentUpdate = null;
    }
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  // ===== Drafting =====

  pickCard(index: number) {
    if (this.waitingForPack()) return;
    this.websocketService.send({
      type: MessageType.DRAFT_PICK,
      cardIndex: index
    });
    this.waitingForPack.set(true);
  }

  get passDirection(): string {
    return (this.packNumber() % 2 === 0) ? 'Left' : 'Right';
  }

  get passDirectionArrow(): string {
    return (this.packNumber() % 2 === 0) ? '\u2190' : '\u2192';
  }

  // ===== Deck Building =====

  toggleCardInDeck(index: number) {
    if (this.deckSubmitted()) return;
    const current = new Set(this.deckCards());
    if (current.has(index)) {
      current.delete(index);
    } else {
      current.add(index);
    }
    this.deckCards.set(current);
  }

  isInDeck(index: number): boolean {
    return this.deckCards().has(index);
  }

  adjustLand(landName: string, delta: number) {
    if (this.deckSubmitted()) return;
    const current = {...this.basicLands()};
    current[landName] = Math.max(0, (current[landName] || 0) + delta);
    this.basicLands.set(current);
  }

  get totalLands(): number {
    return Object.values(this.basicLands()).reduce((a, b) => a + b, 0);
  }

  get totalDeckSize(): number {
    return this.deckCards().size + this.totalLands;
  }

  get canSubmitDeck(): boolean {
    return this.totalDeckSize >= 40 && !this.deckSubmitted();
  }

  autoSuggestLands() {
    if (this.deckSubmitted()) return;
    const pool = this.draftPool();
    const deckIndices = this.deckCards();

    const colorCounts: Record<string, number> = {W: 0, U: 0, B: 0, R: 0, G: 0};
    for (const idx of deckIndices) {
      const card = pool[idx];
      if (card.manaCost) {
        const cost = card.manaCost;
        for (const [code, _] of Object.entries(colorCounts)) {
          const regex = new RegExp(`\\{${code}\\}`, 'g');
          const matches = cost.match(regex);
          if (matches) colorCounts[code] += matches.length;
        }
      }
    }

    const totalSymbols = Object.values(colorCounts).reduce((a, b) => a + b, 0);
    const lands: Record<string, number> = {Plains: 0, Island: 0, Swamp: 0, Mountain: 0, Forest: 0};
    const colorToLand: Record<string, string> = {W: 'Plains', U: 'Island', B: 'Swamp', R: 'Mountain', G: 'Forest'};

    if (totalSymbols === 0) {
      lands['Plains'] = 9;
      lands['Island'] = 8;
    } else {
      let remaining = 17;
      const entries = Object.entries(colorCounts).filter(([_, v]) => v > 0);
      for (const [code, count] of entries) {
        const landName = colorToLand[code];
        const amount = Math.max(1, Math.round(count / totalSymbols * 17));
        lands[landName] = amount;
        remaining -= amount;
      }
      // Adjust to exactly 17
      if (remaining !== 0 && entries.length > 0) {
        const primaryLand = colorToLand[entries.sort((a, b) => b[1] - a[1])[0][0]];
        lands[primaryLand] = Math.max(0, lands[primaryLand] + remaining);
      }
    }

    this.basicLands.set(lands);
  }

  submitDeck() {
    if (!this.canSubmitDeck) return;
    this.websocketService.send({
      type: MessageType.SUBMIT_DECK,
      cardIndices: Array.from(this.deckCards()),
      basicLands: this.basicLands()
    });
    this.deckSubmitted.set(true);
  }

  private startTimer() {
    if (this.timerInterval) clearInterval(this.timerInterval);
    this.updateTimer();
    this.timerInterval = setInterval(() => this.updateTimer(), 1000);
  }

  private updateTimer() {
    const remaining = Math.max(0, this.deadlineEpochMillis() - Date.now());
    const minutes = Math.floor(remaining / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    this.timerDisplay.set(`${minutes}:${seconds.toString().padStart(2, '0')}`);
    if (remaining <= 0 && this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  // ===== Tournament =====

  backToHome() {
    this.router.navigate(['/home']);
  }

  // ===== Pool grouping =====

  get poolByColor(): {color: string, cards: {card: Card, index: number}[]}[] {
    const pool = this.draftPool();
    const groups: Record<string, {card: Card, index: number}[]> = {
      'WHITE': [], 'BLUE': [], 'BLACK': [], 'RED': [], 'GREEN': [], 'COLORLESS': [], 'MULTI': []
    };

    pool.forEach((card, index) => {
      const colors = card.color;
      if (!colors) {
        groups['COLORLESS'].push({card, index});
      } else {
        const colorKey = colors.toUpperCase();
        if (groups[colorKey]) {
          groups[colorKey].push({card, index});
        } else {
          groups['MULTI'].push({card, index});
        }
      }
    });

    return Object.entries(groups)
      .filter(([_, cards]) => cards.length > 0)
      .map(([color, cards]) => ({color, cards}));
  }

  get poolByColorGrouped(): {color: string, cards: {card: Card, count: number}[]}[] {
    return this.poolByColor.map(group => {
      const counts = new Map<string, {card: Card, count: number}>();
      for (const item of group.cards) {
        const existing = counts.get(item.card.name);
        if (existing) {
          existing.count++;
        } else {
          counts.set(item.card.name, {card: item.card, count: 1});
        }
      }
      return {color: group.color, cards: Array.from(counts.values())};
    });
  }

  readonly landNames = ['Plains', 'Island', 'Swamp', 'Mountain', 'Forest'];

  readonly landColors: Record<string, string> = {
    'Plains': '#f9f3e3',
    'Island': '#c4dff6',
    'Swamp': '#cbc2d6',
    'Mountain': '#f4c4b0',
    'Forest': '#c4deb8'
  };
}
