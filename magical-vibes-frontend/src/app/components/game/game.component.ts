import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameStatus, MessageType, TurnStep, PHASE_GROUPS, PhaseGroup } from '../../services/websocket.service';
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

        if ((notification.type === MessageType.OPPONENT_JOINED || notification.type === MessageType.GAME_STATE_UPDATED) && notification.game) {
          this.game.set(notification.game);
          this.websocketService.currentGame = notification.game;
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

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY, gameId: g.id });
    }
  }

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
