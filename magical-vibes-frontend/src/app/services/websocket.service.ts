import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export enum MessageType {
  LOGIN = 'LOGIN',
  CREATE_GAME = 'CREATE_GAME',
  JOIN_GAME = 'JOIN_GAME',
  LOGIN_SUCCESS = 'LOGIN_SUCCESS',
  LOGIN_FAILURE = 'LOGIN_FAILURE',
  TIMEOUT = 'TIMEOUT',
  GAME_JOINED = 'GAME_JOINED',
  OPPONENT_JOINED = 'OPPONENT_JOINED',
  NEW_GAME = 'NEW_GAME',
  GAME_UPDATED = 'GAME_UPDATED',
  PASS_PRIORITY = 'PASS_PRIORITY',
  PRIORITY_UPDATED = 'PRIORITY_UPDATED',
  STEP_ADVANCED = 'STEP_ADVANCED',
  TURN_CHANGED = 'TURN_CHANGED',
  GAME_LOG_ENTRY = 'GAME_LOG_ENTRY',
  KEEP_HAND = 'KEEP_HAND',
  TAKE_MULLIGAN = 'TAKE_MULLIGAN',
  HAND_DRAWN = 'HAND_DRAWN',
  MULLIGAN_RESOLVED = 'MULLIGAN_RESOLVED',
  GAME_STARTED = 'GAME_STARTED',
  ERROR = 'ERROR'
}

export enum GameStatus {
  WAITING = 'WAITING',
  MULLIGAN = 'MULLIGAN',
  RUNNING = 'RUNNING',
  FINISHED = 'FINISHED'
}

export enum TurnStep {
  UNTAP = 'UNTAP',
  UPKEEP = 'UPKEEP',
  DRAW = 'DRAW',
  PRECOMBAT_MAIN = 'PRECOMBAT_MAIN',
  BEGINNING_OF_COMBAT = 'BEGINNING_OF_COMBAT',
  DECLARE_ATTACKERS = 'DECLARE_ATTACKERS',
  DECLARE_BLOCKERS = 'DECLARE_BLOCKERS',
  COMBAT_DAMAGE = 'COMBAT_DAMAGE',
  END_OF_COMBAT = 'END_OF_COMBAT',
  POSTCOMBAT_MAIN = 'POSTCOMBAT_MAIN',
  END_STEP = 'END_STEP',
  CLEANUP = 'CLEANUP'
}

export interface TurnStepInfo {
  step: TurnStep;
  displayName: string;
  phaseName: string;
}

export const TURN_STEPS: TurnStepInfo[] = [
  { step: TurnStep.UNTAP, displayName: 'Untap', phaseName: 'Beginning Phase' },
  { step: TurnStep.UPKEEP, displayName: 'Upkeep', phaseName: 'Beginning Phase' },
  { step: TurnStep.DRAW, displayName: 'Draw', phaseName: 'Beginning Phase' },
  { step: TurnStep.PRECOMBAT_MAIN, displayName: 'Precombat Main', phaseName: 'Precombat Main Phase' },
  { step: TurnStep.BEGINNING_OF_COMBAT, displayName: 'Beginning of Combat', phaseName: 'Combat Phase' },
  { step: TurnStep.DECLARE_ATTACKERS, displayName: 'Declare Attackers', phaseName: 'Combat Phase' },
  { step: TurnStep.DECLARE_BLOCKERS, displayName: 'Declare Blockers', phaseName: 'Combat Phase' },
  { step: TurnStep.COMBAT_DAMAGE, displayName: 'Combat Damage', phaseName: 'Combat Phase' },
  { step: TurnStep.END_OF_COMBAT, displayName: 'End of Combat', phaseName: 'Combat Phase' },
  { step: TurnStep.POSTCOMBAT_MAIN, displayName: 'Postcombat Main', phaseName: 'Postcombat Main Phase' },
  { step: TurnStep.END_STEP, displayName: 'End Step', phaseName: 'Ending Phase' },
  { step: TurnStep.CLEANUP, displayName: 'Cleanup', phaseName: 'Ending Phase' },
];

export interface PhaseGroup {
  phaseName: string;
  steps: TurnStepInfo[];
}

export const PHASE_GROUPS: PhaseGroup[] = TURN_STEPS.reduce<PhaseGroup[]>((groups, stepInfo) => {
  const existing = groups.find(g => g.phaseName === stepInfo.phaseName);
  if (existing) {
    existing.steps.push(stepInfo);
  } else {
    groups.push({ phaseName: stepInfo.phaseName, steps: [stepInfo] });
  }
  return groups;
}, []);

export interface Card {
  name: string;
  type: string;
  subtype: string;
  manaProduced: string;
}

export interface Game {
  id: number;
  gameName: string;
  status: GameStatus;
  playerNames: string[];
  playerIds: number[];
  gameLog: string[];
  currentStep: TurnStep | null;
  activePlayerId: number | null;
  turnNumber: number;
  priorityPlayerId: number | null;
  hand: Card[];
  mulliganCount: number;
}

export interface LobbyGame {
  id: number;
  gameName: string;
  createdByUsername: string;
  playerCount: number;
  status: GameStatus;
}

export interface LoginResponse {
  type: MessageType;
  message: string;
  userId?: number;
  username?: string;
  games?: LobbyGame[];
}

export interface GameNotification {
  type: MessageType;
  message?: string;
  game?: Game;
}

export interface LobbyGameNotification {
  type: MessageType;
  game?: LobbyGame;
}

export interface GameUpdate {
  type: MessageType;
  priorityPlayerId?: number;
  currentStep?: TurnStep;
  activePlayerId?: number;
  turnNumber?: number;
}

export interface HandDrawnNotification {
  type: MessageType;
  hand: Card[];
  mulliganCount: number;
}

export interface MulliganResolvedNotification {
  type: MessageType;
  playerName: string;
  kept: boolean;
  mulliganCount: number;
}

export interface GameStartedNotification {
  type: MessageType;
  activePlayerId: number;
  turnNumber: number;
  currentStep: TurnStep;
  priorityPlayerId: number;
}

export type WebSocketMessage = LoginResponse | GameNotification | LobbyGameNotification | GameUpdate | HandDrawnNotification | MulliganResolvedNotification | GameStartedNotification;

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
  initialGames: LobbyGame[] = [];

  login(username: string, password: string): Observable<LoginResponse> {
    return new Observable(observer => {
      this.authenticated = false;
      this.currentUser = null;
      this.currentGame = null;
      this.initialGames = [];

      this.ws = new WebSocket(this.WS_URL);

      this.ws.onopen = () => {
        this.ws!.send(JSON.stringify({
          type: MessageType.LOGIN,
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
            if (response.type === MessageType.LOGIN_SUCCESS) {
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
