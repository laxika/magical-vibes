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
  SELECT_CARDS_TO_BOTTOM = 'SELECT_CARDS_TO_BOTTOM',
  BOTTOM_CARDS = 'BOTTOM_CARDS',
  DECK_SIZES_UPDATED = 'DECK_SIZES_UPDATED',
  PLAYABLE_CARDS_UPDATED = 'PLAYABLE_CARDS_UPDATED',
  PLAY_CARD = 'PLAY_CARD',
  BATTLEFIELD_UPDATED = 'BATTLEFIELD_UPDATED',
  TAP_PERMANENT = 'TAP_PERMANENT',
  MANA_UPDATED = 'MANA_UPDATED',
  SET_AUTO_STOPS = 'SET_AUTO_STOPS',
  AUTO_STOPS_UPDATED = 'AUTO_STOPS_UPDATED',
  DECLARE_ATTACKERS = 'DECLARE_ATTACKERS',
  DECLARE_BLOCKERS = 'DECLARE_BLOCKERS',
  AVAILABLE_ATTACKERS = 'AVAILABLE_ATTACKERS',
  AVAILABLE_BLOCKERS = 'AVAILABLE_BLOCKERS',
  LIFE_UPDATED = 'LIFE_UPDATED',
  GAME_OVER = 'GAME_OVER',
  CHOOSE_CARD_FROM_HAND = 'CHOOSE_CARD_FROM_HAND',
  CARD_CHOSEN = 'CARD_CHOSEN',
  STACK_UPDATED = 'STACK_UPDATED',
  GRAVEYARD_UPDATED = 'GRAVEYARD_UPDATED',
  CHOOSE_COLOR = 'CHOOSE_COLOR',
  COLOR_CHOSEN = 'COLOR_CHOSEN',
  MAY_ABILITY_CHOICE = 'MAY_ABILITY_CHOICE',
  MAY_ABILITY_CHOSEN = 'MAY_ABILITY_CHOSEN',
  ACTIVATE_ABILITY = 'ACTIVATE_ABILITY',
  CHOOSE_PERMANENT = 'CHOOSE_PERMANENT',
  PERMANENT_CHOSEN = 'PERMANENT_CHOSEN',
  CHOOSE_MULTIPLE_PERMANENTS = 'CHOOSE_MULTIPLE_PERMANENTS',
  MULTIPLE_PERMANENTS_CHOSEN = 'MULTIPLE_PERMANENTS_CHOSEN',
  REORDER_LIBRARY_CARDS = 'REORDER_LIBRARY_CARDS',
  LIBRARY_CARDS_REORDERED = 'LIBRARY_CARDS_REORDERED',
  REVEAL_HAND = 'REVEAL_HAND',
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

export interface ActivatedAbilityView {
  description: string;
  requiresTap: boolean;
  needsTarget: boolean;
  targetsPlayer: boolean;
  allowedTargetTypes: string[];
  manaCost: string | null;
}

export interface Card {
  name: string;
  type: string;
  supertypes: string[];
  subtypes: string[];
  cardText: string | null;
  manaCost: string | null;
  power: number | null;
  toughness: number | null;
  keywords: string[];
  hasTapAbility: boolean;
  setCode: string | null;
  collectorNumber: string | null;
  flavorText: string | null;
  artist: string | null;
  rarity: string | null;
  color: string | null;
  needsTarget: boolean;
  needsSpellTarget: boolean;
  targetsPlayer: boolean;
  requiresAttackingTarget: boolean;
  allowedTargetTypes: string[];
  activatedAbilities: ActivatedAbilityView[];
}

export interface Permanent {
  id: string;
  card: Card;
  tapped: boolean;
  attacking: boolean;
  blocking: boolean;
  blockingTargets: number[];
  summoningSick: boolean;
  powerModifier: number;
  toughnessModifier: number;
  grantedKeywords: string[];
  effectivePower: number;
  effectiveToughness: number;
  chosenColor: string | null;
  regenerationShield: number;
  attachedTo: string | null;
  cantBeBlocked: boolean;
  animatedCreature: boolean;
}

export interface StackEntry {
  entryType: string;
  card: Card;
  controllerId: string;
  description: string;
  cardId: string;
  isSpell: boolean;
  targetPermanentId: string | null;
}

export interface Game {
  id: string;
  gameName: string;
  status: GameStatus;
  playerNames: string[];
  playerIds: string[];
  gameLog: string[];
  currentStep: TurnStep | null;
  activePlayerId: string | null;
  turnNumber: number;
  priorityPlayerId: string | null;
  hand: Card[];
  mulliganCount: number;
  deckSizes: number[];
  battlefields: Permanent[][];
  manaPool: Record<string, number>;
  autoStopSteps: string[];
  lifeTotals: number[];
  stack: StackEntry[];
  graveyards: Card[][];
}

export interface LobbyGame {
  id: string;
  gameName: string;
  createdByUsername: string;
  playerCount: number;
  status: GameStatus;
}

export interface DeckInfo {
  id: string;
  name: string;
}

export interface LoginResponse {
  type: MessageType;
  message: string;
  userId?: string;
  username?: string;
  games?: LobbyGame[];
  decks?: DeckInfo[];
  activeGame?: Game;
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
  priorityPlayerId?: string;
  currentStep?: TurnStep;
  activePlayerId?: string;
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
  activePlayerId: string;
  turnNumber: number;
  currentStep: TurnStep;
  priorityPlayerId: string;
}

export interface SelectCardsToBottomNotification {
  type: MessageType;
  count: number;
}

export interface DeckSizesUpdatedNotification {
  type: MessageType;
  deckSizes: number[];
}

export interface PlayableCardsNotification {
  type: MessageType;
  playableCardIndices: number[];
}

export interface BattlefieldUpdatedNotification {
  type: MessageType;
  battlefields: Permanent[][];
}

export interface ManaUpdatedNotification {
  type: MessageType;
  manaPool: Record<string, number>;
}

export interface AutoStopsUpdatedNotification {
  type: MessageType;
  autoStopSteps: string[];
}

export interface AvailableAttackersNotification {
  type: MessageType;
  attackerIndices: number[];
}

export interface AvailableBlockersNotification {
  type: MessageType;
  blockerIndices: number[];
  attackerIndices: number[];
}

export interface LifeUpdatedNotification {
  type: MessageType;
  lifeTotals: number[];
}

export interface GameOverNotification {
  type: MessageType;
  winnerId: string;
  winnerName: string;
}

export interface ChooseCardFromHandNotification {
  type: MessageType;
  cardIndices: number[];
  prompt: string;
}

export interface ChooseColorNotification {
  type: MessageType;
  colors: string[];
  prompt: string;
}

export interface MayAbilityNotification {
  type: MessageType;
  prompt: string;
}

export interface ChoosePermanentNotification {
  type: MessageType;
  permanentIds: string[];
  prompt: string;
}

export interface ChooseMultiplePermanentsNotification {
  type: MessageType;
  permanentIds: string[];
  maxCount: number;
  prompt: string;
}

export interface StackUpdatedNotification {
  type: MessageType;
  stack: StackEntry[];
}

export interface GraveyardUpdatedNotification {
  type: MessageType;
  graveyards: Card[][];
}

export interface ReorderLibraryCardsNotification {
  type: MessageType;
  cards: Card[];
  prompt: string;
}

export interface RevealHandNotification {
  type: MessageType;
  cards: Card[];
  playerName: string;
}

export type WebSocketMessage = LoginResponse | GameNotification | LobbyGameNotification | GameUpdate | HandDrawnNotification | MulliganResolvedNotification | GameStartedNotification | SelectCardsToBottomNotification | DeckSizesUpdatedNotification | PlayableCardsNotification | BattlefieldUpdatedNotification | ManaUpdatedNotification | AutoStopsUpdatedNotification | AvailableAttackersNotification | AvailableBlockersNotification | LifeUpdatedNotification | GameOverNotification | ChooseCardFromHandNotification | ChooseColorNotification | MayAbilityNotification | ChoosePermanentNotification | ChooseMultiplePermanentsNotification | StackUpdatedNotification | GraveyardUpdatedNotification | ReorderLibraryCardsNotification | RevealHandNotification;

export interface User {
  userId: string;
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
  availableDecks: DeckInfo[] = [];

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
              this.availableDecks = response.decks ?? [];
              if (response.activeGame) {
                this.currentGame = response.activeGame;
              }
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
    this.availableDecks = [];
    this.authenticated = false;
  }
}
