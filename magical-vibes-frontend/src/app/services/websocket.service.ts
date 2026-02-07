import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface Game {
  id: number;
  gameName: string;
  createdByUsername: string;
  status: string;
  createdAt: string;
  playerCount: number;
  playerNames: string[];
}

export interface LoginResponse {
  type: string;
  message: string;
  userId?: number;
  username?: string;
  games?: Game[];
}

export interface GameNotification {
  type: 'NEW_GAME' | 'GAME_UPDATED' | 'GAME_JOINED' | 'OPPONENT_JOINED' | 'ERROR';
  message?: string;
  game?: Game;
}

export type WebSocketMessage = LoginResponse | GameNotification;

export interface User {
  userId: number;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private readonly WS_URL = 'ws://localhost:8080/ws/login';
  private ws: WebSocket | null = null;
  private messages = new Subject<WebSocketMessage>();
  private disconnected = new Subject<void>();
  private authenticated = false;

  currentUser: User | null = null;
  currentGame: Game | null = null;
  initialGames: Game[] = [];

  login(username: string, password: string): Observable<LoginResponse> {
    return new Observable(observer => {
      this.authenticated = false;
      this.currentUser = null;
      this.currentGame = null;
      this.initialGames = [];

      this.ws = new WebSocket(this.WS_URL);

      this.ws.onopen = () => {
        this.ws!.send(JSON.stringify({
          type: 'LOGIN',
          username: username,
          password: password
        }));
      };

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);

          // Before authenticated, only handle login responses
          if (!this.authenticated) {
            const response = message as LoginResponse;
            if (response.type === 'LOGIN_SUCCESS') {
              this.authenticated = true;
              this.currentUser = { userId: response.userId!, username: response.username! };
              this.initialGames = response.games ?? [];
            }
            observer.next(response);
            observer.complete();
            return;
          }

          // After authenticated, forward everything to the messages stream
          this.messages.next(message);
        } catch (error) {
          observer.error('Failed to parse response');
        }
      };

      this.ws.onerror = () => {
        if (this.authenticated) {
          this.cleanup();
          this.disconnected.next();
        } else {
          observer.error('WebSocket connection error');
          this.cleanup();
        }
      };

      this.ws.onclose = () => {
        if (this.authenticated) {
          this.cleanup();
          this.disconnected.next();
        } else if (!observer.closed) {
          observer.complete();
          this.cleanup();
        }
      };
    });
  }

  send(message: object): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    }
  }

  getMessages(): Observable<WebSocketMessage> {
    return this.messages.asObservable();
  }

  onDisconnected(): Observable<void> {
    return this.disconnected.asObservable();
  }

  disconnect(): void {
    this.authenticated = false;
    if (this.ws) {
      // Remove onclose handler to avoid triggering redirect on intentional disconnect
      this.ws.onclose = null;
      this.ws.close();
    }
    this.cleanup();
  }

  isConnected(): boolean {
    return this.authenticated && this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }

  private cleanup(): void {
    this.ws = null;
    this.currentUser = null;
    this.currentGame = null;
    this.initialGames = [];
    this.authenticated = false;
  }
}
