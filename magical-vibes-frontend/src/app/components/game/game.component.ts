import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameStatus, MessageType } from '../../services/websocket.service';
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

        if (notification.type === MessageType.OPPONENT_JOINED && notification.game) {
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

  readonly GameStatus = GameStatus;
}
