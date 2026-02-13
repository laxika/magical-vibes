import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, GameNotification, LobbyGame, LobbyGameNotification, DeckInfo, GameStatus, MessageType } from '../../services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy {
  games = signal<LobbyGame[]>([]);
  newGameName = signal('');
  selectedDeckId = signal<string>('');
  errorMessage = signal('');
  showCreateForm = signal(false);
  private subscriptions: Subscription[] = [];

  constructor(
    private router: Router,
    public websocketService: WebsocketService
  ) {}

  ngOnInit() {
    // No active connection means no session - go back to login
    if (!this.websocketService.isConnected()) {
      this.router.navigate(['/']);
      return;
    }

    // Load initial games received with login response
    this.games.set(this.websocketService.initialGames);

    // Initialize selected deck to first available deck
    if (this.websocketService.availableDecks.length > 0) {
      this.selectedDeckId.set(this.websocketService.availableDecks[0].id);
    }

    // Listen for game notifications
    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        if (message.type === MessageType.GAME_JOINED) {
          const notification = message as GameNotification;
          if (notification.game) {
            this.websocketService.currentGame = notification.game;
            this.router.navigate(['/game']);
          }
        } else if (message.type === MessageType.NEW_GAME) {
          const notification = message as LobbyGameNotification;
          if (notification.game) {
            const currentGames = this.games();
            if (!currentGames.some(g => g.id === notification.game!.id)) {
              this.games.set([...currentGames, notification.game]);
            }
          }
        } else if (message.type === MessageType.GAME_UPDATED) {
          const notification = message as LobbyGameNotification;
          if (notification.game) {
            const currentGames = this.games();
            const index = currentGames.findIndex(g => g.id === notification.game!.id);
            if (index !== -1) {
              const updatedGames = [...currentGames];
              updatedGames[index] = notification.game;
              this.games.set(updatedGames);
            }
          }
        } else if (message.type === MessageType.ERROR) {
          const notification = message as GameNotification;
          if (notification.message) {
            this.errorMessage.set(notification.message);
          }
        }
      })
    );

    // If WebSocket drops, redirect to login
    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        this.router.navigate(['/']);
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  toggleCreateForm() {
    this.showCreateForm.set(!this.showCreateForm());
    this.newGameName.set('');
    this.errorMessage.set('');
  }

  createGame() {
    if (!this.newGameName().trim()) {
      this.errorMessage.set('Please enter a game name');
      return;
    }

    this.errorMessage.set('');
    this.websocketService.send({
      type: MessageType.CREATE_GAME,
      gameName: this.newGameName(),
      deckId: this.selectedDeckId()
    });

    this.newGameName.set('');
    this.showCreateForm.set(false);
  }

  joinGame(gameId: string) {
    this.errorMessage.set('');
    this.websocketService.send({
      type: MessageType.JOIN_GAME,
      gameId: gameId,
      deckId: this.selectedDeckId()
    });
  }

  logout() {
    this.websocketService.disconnect();
    this.router.navigate(['/']);
  }

  get currentUser() {
    return this.websocketService.currentUser;
  }

  readonly GameStatus = GameStatus;
}
