import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameUpdate, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, HandDrawnNotification, MulliganResolvedNotification, GameStartedNotification } from '../../services/websocket.service';
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

    const opponentName = g.playerNames.find(n => n !== this.websocketService.currentUser?.username);
    if (resolved.playerName === opponentName) {
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
  }

  keepHand(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.KEEP_HAND, gameId: g.id });
    }
  }

  takeMulligan(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.TAKE_MULLIGAN, gameId: g.id });
    }
  }

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY, gameId: g.id });
    }
  }

  opponentKept = false;

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
