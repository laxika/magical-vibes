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
  GAME_STATE = 'GAME_STATE',
  KEEP_HAND = 'KEEP_HAND',
  TAKE_MULLIGAN = 'TAKE_MULLIGAN',
  MULLIGAN_RESOLVED = 'MULLIGAN_RESOLVED',
  SELECT_CARDS_TO_BOTTOM = 'SELECT_CARDS_TO_BOTTOM',
  BOTTOM_CARDS = 'BOTTOM_CARDS',
  PLAY_CARD = 'PLAY_CARD',
  TAP_PERMANENT = 'TAP_PERMANENT',
  SACRIFICE_PERMANENT = 'SACRIFICE_PERMANENT',
  SET_AUTO_STOPS = 'SET_AUTO_STOPS',
  DECLARE_ATTACKERS = 'DECLARE_ATTACKERS',
  DECLARE_BLOCKERS = 'DECLARE_BLOCKERS',
  AVAILABLE_ATTACKERS = 'AVAILABLE_ATTACKERS',
  AVAILABLE_BLOCKERS = 'AVAILABLE_BLOCKERS',
  GAME_OVER = 'GAME_OVER',
  CHOOSE_CARD_FROM_HAND = 'CHOOSE_CARD_FROM_HAND',
  CARD_CHOSEN = 'CARD_CHOSEN',
  CHOOSE_COLOR = 'CHOOSE_COLOR',
  COLOR_CHOSEN = 'COLOR_CHOSEN',
  MAY_ABILITY_CHOICE = 'MAY_ABILITY_CHOICE',
  MAY_ABILITY_CHOSEN = 'MAY_ABILITY_CHOSEN',
  ACTIVATE_ABILITY = 'ACTIVATE_ABILITY',
  CHOOSE_PERMANENT = 'CHOOSE_PERMANENT',
  PERMANENT_CHOSEN = 'PERMANENT_CHOSEN',
  CHOOSE_MULTIPLE_PERMANENTS = 'CHOOSE_MULTIPLE_PERMANENTS',
  MULTIPLE_PERMANENTS_CHOSEN = 'MULTIPLE_PERMANENTS_CHOSEN',
  CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS = 'CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS',
  MULTIPLE_GRAVEYARD_CARDS_CHOSEN = 'MULTIPLE_GRAVEYARD_CARDS_CHOSEN',
  REORDER_LIBRARY_CARDS = 'REORDER_LIBRARY_CARDS',
  LIBRARY_CARDS_REORDERED = 'LIBRARY_CARDS_REORDERED',
  CHOOSE_CARD_FROM_LIBRARY = 'CHOOSE_CARD_FROM_LIBRARY',
  LIBRARY_CARD_CHOSEN = 'LIBRARY_CARD_CHOSEN',
  REVEAL_HAND = 'REVEAL_HAND',
  CHOOSE_FROM_REVEALED_HAND = 'CHOOSE_FROM_REVEALED_HAND',
  CHOOSE_CARD_FROM_GRAVEYARD = 'CHOOSE_CARD_FROM_GRAVEYARD',
  GRAVEYARD_CARD_CHOSEN = 'GRAVEYARD_CARD_CHOSEN',
  CHOOSE_HAND_TOP_BOTTOM = 'CHOOSE_HAND_TOP_BOTTOM',
  HAND_TOP_BOTTOM_CHOSEN = 'HAND_TOP_BOTTOM_CHOSEN',
  ERROR = 'ERROR',
  CREATE_DRAFT = 'CREATE_DRAFT',
  DRAFT_JOINED = 'DRAFT_JOINED',
  DRAFT_PACK_UPDATE = 'DRAFT_PACK_UPDATE',
  DRAFT_PICK = 'DRAFT_PICK',
  DECK_BUILDING_STATE = 'DECK_BUILDING_STATE',
  SUBMIT_DECK = 'SUBMIT_DECK',
  TOURNAMENT_UPDATE = 'TOURNAMENT_UPDATE',
  TOURNAMENT_GAME_READY = 'TOURNAMENT_GAME_READY',
  DRAFT_FINISHED = 'DRAFT_FINISHED',
  COMBAT_DAMAGE_ASSIGNMENT = 'COMBAT_DAMAGE_ASSIGNMENT',
  COMBAT_DAMAGE_ASSIGNED = 'COMBAT_DAMAGE_ASSIGNED'
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
  needsSpellTarget: boolean;
  targetsPlayer: boolean;
  allowedTargetTypes: string[];
  allowedTargetColors: string[];
  manaCost: string | null;
  loyaltyCost: number | null;
  targetsBlockingThis: boolean;
}

export interface Card {
  name: string;
  type: string;
  additionalTypes: string[];
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
  color: string | null;
  needsTarget: boolean;
  needsSpellTarget: boolean;
  targetsPlayer: boolean;
  requiresAttackingTarget: boolean;
  allowedTargetTypes: string[];
  allowedTargetSubtypes: string[];
  activatedAbilities: ActivatedAbilityView[];
  loyalty: number | null;
  minTargets: number;
  maxTargets: number;
  hasConvoke: boolean;
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
  loyaltyCounters: number;
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
  opponentHand: Card[];
  mulliganCount: number;
  deckSizes: number[];
  handSizes: number[];
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

export interface SetInfo {
  code: string;
  name: string;
}

export interface LoginResponse {
  type: MessageType;
  message: string;
  userId?: string;
  username?: string;
  games?: LobbyGame[];
  decks?: DeckInfo[];
  sets?: SetInfo[];
  activeGame?: Game;
  activeDraftId?: string;
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

export interface GameStateNotification {
  type: MessageType;
  status: GameStatus;
  activePlayerId: string;
  turnNumber: number;
  currentStep: TurnStep;
  priorityPlayerId: string;
  battlefields: Permanent[][];
  stack: StackEntry[];
  graveyards: Card[][];
  deckSizes: number[];
  handSizes: number[];
  lifeTotals: number[];
  hand: Card[];
  opponentHand: Card[];
  mulliganCount: number;
  manaPool: Record<string, number>;
  autoStopSteps: string[];
  playableCardIndices: number[];
  playableGraveyardLandIndices: number[];
  newLogEntries: string[];
}

export interface MulliganResolvedNotification {
  type: MessageType;
  playerName: string;
  kept: boolean;
  mulliganCount: number;
}

export interface SelectCardsToBottomNotification {
  type: MessageType;
  count: number;
}

export interface AvailableAttackersNotification {
  type: MessageType;
  attackerIndices: number[];
  mustAttackIndices: number[];
}

export interface AvailableBlockersNotification {
  type: MessageType;
  blockerIndices: number[];
  attackerIndices: number[];
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
  playerIds?: string[];
  prompt: string;
}

export interface ChooseMultiplePermanentsNotification {
  type: MessageType;
  permanentIds: string[];
  maxCount: number;
  prompt: string;
}

export interface ChooseMultipleCardsFromGraveyardsNotification {
  type: MessageType;
  cardIds: string[];
  cards: Card[];
  maxCount: number;
  prompt: string;
}

export interface ReorderLibraryCardsNotification {
  type: MessageType;
  cards: Card[];
  prompt: string;
}

export interface ChooseCardFromLibraryNotification {
  type: MessageType;
  cards: Card[];
  prompt: string;
  canFailToFind: boolean;
}

export interface RevealHandNotification {
  type: MessageType;
  cards: Card[];
  playerName: string;
}

export interface ChooseFromRevealedHandNotification {
  type: MessageType;
  cards: Card[];
  validIndices: number[];
  prompt: string;
}

export interface ChooseCardFromGraveyardNotification {
  type: MessageType;
  cardIndices: number[];
  prompt: string;
  allGraveyards: boolean;
}

export interface ChooseHandTopBottomNotification {
  type: MessageType;
  cards: Card[];
  prompt: string;
}

export enum DraftStatus {
  WAITING = 'WAITING',
  DRAFTING = 'DRAFTING',
  DECK_BUILDING = 'DECK_BUILDING',
  TOURNAMENT = 'TOURNAMENT',
  FINISHED = 'FINISHED'
}

export interface DraftJoinedNotification {
  type: MessageType;
  draftId: string;
  draftName: string;
  setCode: string;
  playerNames: string[];
  status: string;
}

export interface DraftPackUpdateNotification {
  type: MessageType;
  pack: Card[];
  packNumber: number;
  pickNumber: number;
  pool: Card[];
}

export interface DeckBuildingStateNotification {
  type: MessageType;
  pool: Card[];
  deadlineEpochMillis: number;
  alreadySubmitted?: boolean;
}

export interface TournamentPairing {
  player1Name: string;
  player2Name: string;
  winnerName: string | null;
}

export interface TournamentRound {
  roundName: string;
  pairings: TournamentPairing[];
}

export interface TournamentUpdateNotification {
  type: MessageType;
  rounds: TournamentRound[];
  currentRound: number;
  roundName: string;
}

export interface TournamentGameReadyNotification {
  type: MessageType;
  gameId: string;
  opponentName: string;
}

export interface DraftFinishedNotification {
  type: MessageType;
  winnerName: string;
}

export interface CombatDamageTargetView {
  id: string;
  name: string;
  toughness: number;
  currentDamage: number;
  isPlayer: boolean;
}

export interface CombatDamageAssignmentNotification {
  type: MessageType;
  attackerIndex: number;
  attackerPermanentId: string;
  attackerName: string;
  totalDamage: number;
  validTargets: CombatDamageTargetView[];
  isTrample: boolean;
}

export type WebSocketMessage = LoginResponse | GameNotification | LobbyGameNotification | GameStateNotification | MulliganResolvedNotification | SelectCardsToBottomNotification | AvailableAttackersNotification | AvailableBlockersNotification | GameOverNotification | ChooseCardFromHandNotification | ChooseColorNotification | MayAbilityNotification | ChoosePermanentNotification | ChooseMultiplePermanentsNotification | ChooseMultipleCardsFromGraveyardsNotification | ReorderLibraryCardsNotification | ChooseCardFromLibraryNotification | RevealHandNotification | ChooseFromRevealedHandNotification | ChooseCardFromGraveyardNotification | ChooseHandTopBottomNotification | DraftJoinedNotification | DraftPackUpdateNotification | DeckBuildingStateNotification | TournamentUpdateNotification | TournamentGameReadyNotification | DraftFinishedNotification | CombatDamageAssignmentNotification;

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
  availableSets: SetInfo[] = [];
  inDraft = false;
  activeDraftId: string | null = null;
  lastDraftJoined: DraftJoinedNotification | null = null;
  lastDraftPackUpdate: DraftPackUpdateNotification | null = null;
  lastDeckBuildingState: DeckBuildingStateNotification | null = null;
  lastTournamentUpdate: TournamentUpdateNotification | null = null;
  lastDraftFinished: DraftFinishedNotification | null = null;

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
              this.availableSets = response.sets ?? [];
              if (response.activeGame) {
                this.currentGame = response.activeGame;
              }
              if (response.activeDraftId) {
                this.activeDraftId = response.activeDraftId;
                this.inDraft = true;
              }
            }
            observer.next(response);
            observer.complete();
            return;
          }

          // Buffer draft state messages so they're available when draft component mounts
          if (message.type === MessageType.DRAFT_JOINED) {
            this.lastDraftJoined = message as DraftJoinedNotification;
          } else if (message.type === MessageType.DRAFT_PACK_UPDATE) {
            this.lastDraftPackUpdate = message as DraftPackUpdateNotification;
          } else if (message.type === MessageType.DECK_BUILDING_STATE) {
            this.lastDeckBuildingState = message as DeckBuildingStateNotification;
          } else if (message.type === MessageType.TOURNAMENT_UPDATE) {
            this.lastTournamentUpdate = message as TournamentUpdateNotification;
          } else if (message.type === MessageType.DRAFT_FINISHED) {
            this.lastDraftFinished = message as DraftFinishedNotification;
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
    this.availableSets = [];
    this.authenticated = false;
    this.inDraft = false;
    this.activeDraftId = null;
    this.lastDraftJoined = null;
    this.lastDraftPackUpdate = null;
    this.lastDeckBuildingState = null;
    this.lastTournamentUpdate = null;
    this.lastDraftFinished = null;
  }
}
