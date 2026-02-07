import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameUpdate, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, HandDrawnNotification, MulliganResolvedNotification, GameStartedNotification, SelectCardsToBottomNotification, DeckSizesUpdatedNotification, PlayableCardsNotification } from '../../services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game.component.html',
  styleUrl: './game.component.css'
})
export class GameComponent implements OnInit, OnDestroy {
  game = signal<Game | null>(null);
  private subscriptions: Subscription[] = [];

  constructor(
    private router: Router,
    private websocketService: WebsocketService
  ) {}

  ngOnInit() {
    if (!this.websocketService.isConnected() || !this.websocketService.currentGame) {
      this.router.navigate(['/']);
      return;
    }

    this.game.set(this.websocketService.currentGame);

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        const notification = message as GameNotification;

        console.log(notification);

        if (notification.type === MessageType.OPPONENT_JOINED && notification.game) {
          this.game.set(notification.game);
          this.websocketService.currentGame = notification.game;
        }

        if (message.type === MessageType.GAME_LOG_ENTRY) {
          this.appendLogEntry((message as GameNotification).message!);
        }

        if (message.type === MessageType.HAND_DRAWN) {
          const handDrawn = message as HandDrawnNotification;
          this.updateHand(handDrawn.hand, handDrawn.mulliganCount);
        }

        if (message.type === MessageType.MULLIGAN_RESOLVED) {
          const resolved = message as MulliganResolvedNotification;
          this.handleMulliganResolved(resolved);
        }

        if (message.type === MessageType.GAME_STARTED) {
          const started = message as GameStartedNotification;
          this.handleGameStarted(started);
        }

        if (message.type === MessageType.SELECT_CARDS_TO_BOTTOM) {
          const selectMsg = message as SelectCardsToBottomNotification;
          this.handleSelectCardsToBottom(selectMsg);
        }

        if (message.type === MessageType.DECK_SIZES_UPDATED) {
          const deckMsg = message as DeckSizesUpdatedNotification;
          this.updateDeckSizes(deckMsg.deckSizes);
        }

        if (message.type === MessageType.PLAYABLE_CARDS_UPDATED) {
          const playableMsg = message as PlayableCardsNotification;
          this.playableCardIndices = new Set(playableMsg.playableCardIndices);
        }

        const update = message as GameUpdate;
        if (update.type === MessageType.PRIORITY_UPDATED ||
            update.type === MessageType.STEP_ADVANCED ||
            update.type === MessageType.TURN_CHANGED) {
          this.applyGameUpdate(update);
        }
      })
    );

    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        this.router.navigate(['/']);
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  get player1Name(): string {
    const g = this.game();
    return g && g.playerNames.length > 0 ? g.playerNames[0] : '';
  }

  get player2Name(): string {
    const g = this.game();
    return g && g.playerNames.length > 1 ? g.playerNames[1] : '';
  }

  get gameLog(): string[] {
    return this.game()?.gameLog ?? [];
  }

  get isWaitingForOpponent(): boolean {
    const g = this.game();
    return g !== null && g.playerNames.length < 2;
  }

  get isMyTurn(): boolean {
    const g = this.game();
    return g !== null && g.activePlayerId === this.websocketService.currentUser?.userId;
  }

  isActivePlayer(playerIndex: number): boolean {
    const g = this.game();
    return g !== null && g.playerIds?.[playerIndex] === g.activePlayerId;
  }

  holdsPriority(playerIndex: number): boolean {
    const g = this.game();
    return g !== null && g.playerIds?.[playerIndex] === g.priorityPlayerId;
  }

  get hasPriority(): boolean {
    const g = this.game();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
  }

  private applyGameUpdate(update: GameUpdate): void {
    const g = this.game();
    if (!g) return;

    // Clear playable highlights immediately on any state change;
    // the server's PLAYABLE_CARDS_UPDATED message will re-set them if appropriate
    this.playableCardIndices = new Set<number>();

    const updated = { ...g };
    if (update.priorityPlayerId !== undefined) {
      updated.priorityPlayerId = update.priorityPlayerId ?? null;
    }
    if (update.currentStep !== undefined) {
      updated.currentStep = update.currentStep;
    }
    if (update.activePlayerId !== undefined) {
      updated.activePlayerId = update.activePlayerId;
    }
    if (update.turnNumber !== undefined) {
      updated.turnNumber = update.turnNumber;
    }
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private appendLogEntry(entry: string): void {
    const g = this.game();
    if (!g) return;

    const updated = { ...g, gameLog: [...g.gameLog, entry] };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  get isMulliganPhase(): boolean {
    const g = this.game();
    return g !== null && g.status === GameStatus.MULLIGAN;
  }

  get canMulligan(): boolean {
    const g = this.game();
    return g !== null && g.mulliganCount < 7;
  }

  get hand(): Card[] {
    return this.game()?.hand ?? [];
  }

  private updateHand(hand: Card[], mulliganCount: number): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, hand, mulliganCount };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleMulliganResolved(resolved: MulliganResolvedNotification): void {
    const g = this.game();
    if (!g) return;

    const myName = this.websocketService.currentUser?.username;
    if (resolved.playerName === myName) {
      if (resolved.kept) {
        this.selfKept = true;
      }
    } else {
      if (resolved.kept) {
        this.opponentKept = true;
      } else {
        this.opponentKept = false;
      }
    }
  }

  private handleGameStarted(started: GameStartedNotification): void {
    const g = this.game();
    if (!g) return;
    const updated = {
      ...g,
      status: GameStatus.RUNNING,
      activePlayerId: started.activePlayerId,
      turnNumber: started.turnNumber,
      currentStep: started.currentStep,
      priorityPlayerId: started.priorityPlayerId
    };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
    this.opponentKept = false;
    this.selfKept = false;
    this.selectingBottomCards = false;
    this.bottomCardCount = 0;
    this.selectedCardIndices.clear();
  }

  private updateDeckSizes(deckSizes: number[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, deckSizes };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  get player1DeckSize(): number {
    return this.game()?.deckSizes?.[0] ?? 0;
  }

  get player2DeckSize(): number {
    return this.game()?.deckSizes?.[1] ?? 0;
  }

  private handleSelectCardsToBottom(msg: SelectCardsToBottomNotification): void {
    this.selectingBottomCards = true;
    this.bottomCardCount = msg.count;
    this.selectedCardIndices.clear();
  }

  keepHand(): void {
    const g = this.game();
    if (g && !this.selfKept) {
      this.websocketService.send({ type: MessageType.KEEP_HAND, gameId: g.id });
    }
  }

  takeMulligan(): void {
    const g = this.game();
    if (g && !this.selfKept) {
      this.websocketService.send({ type: MessageType.TAKE_MULLIGAN, gameId: g.id });
    }
  }

  toggleCardSelection(index: number): void {
    if (this.selectedCardIndices.has(index)) {
      this.selectedCardIndices.delete(index);
    } else if (this.selectedCardIndices.size < this.bottomCardCount) {
      this.selectedCardIndices.add(index);
    }
  }

  isCardSelected(index: number): boolean {
    return this.selectedCardIndices.has(index);
  }

  get canConfirmBottom(): boolean {
    return this.selectedCardIndices.size === this.bottomCardCount;
  }

  confirmBottomCards(): void {
    const g = this.game();
    if (g && this.canConfirmBottom) {
      this.websocketService.send({
        type: MessageType.BOTTOM_CARDS,
        gameId: g.id,
        cardIndices: Array.from(this.selectedCardIndices)
      });
      this.selectingBottomCards = false;
      this.selectedCardIndices.clear();
    }
  }

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY, gameId: g.id });
    }
  }

  opponentKept = false;
  selfKept = false;
  selectingBottomCards = false;
  bottomCardCount = 0;
  selectedCardIndices = new Set<number>();
  playableCardIndices = new Set<number>();

  isCardPlayable(index: number): boolean {
    return this.playableCardIndices.has(index);
  }

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
