import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export enum MessageType {
  LOGIN = 'LOGIN',
  CREATE_GAME = 'CREATE_GAME',
  JOIN_GAME = 'JOIN_GAME',
  LOGIN_SUCCESS = 'LOGIN_SUCCESS',
  LOGIN_FAILURE = 'LOGIN_FAILURE',
  REGISTER = 'REGISTER',
  REGISTER_SUCCESS = 'REGISTER_SUCCESS',
  REGISTER_FAILURE = 'REGISTER_FAILURE',
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
  INTERACTION_PROMPT = 'INTERACTION_PROMPT',
  INTERACTION_ANSWER = 'INTERACTION_ANSWER',
  ACTIVATE_ABILITY = 'ACTIVATE_ABILITY',
  ACTIVATE_GRAVEYARD_ABILITY = 'ACTIVATE_GRAVEYARD_ABILITY',
  ACTIVATE_HAND_ABILITY = 'ACTIVATE_HAND_ABILITY',
  REVEAL_HAND = 'REVEAL_HAND',
  REVEAL_LIBRARY_TOP = 'REVEAL_LIBRARY_TOP',
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
  COMBAT_DAMAGE_ASSIGNED = 'COMBAT_DAMAGE_ASSIGNED',
  REQUEST_CARD_LIST = 'REQUEST_CARD_LIST',
  CARD_LIST_RESPONSE = 'CARD_LIST_RESPONSE',
  VALID_TARGETS_REQUEST = 'VALID_TARGETS_REQUEST',
  VALID_TARGETS_RESPONSE = 'VALID_TARGETS_RESPONSE',
  PAY_SEARCH_TAX = 'PAY_SEARCH_TAX',
  REVERT_MANA_ACTIVATIONS = 'REVERT_MANA_ACTIVATIONS',
  SURRENDER = 'SURRENDER',
  LEAVE_GAME = 'LEAVE_GAME',
  LEAVE_DRAFT = 'LEAVE_DRAFT',
  LOBBY_GAMES_RESPONSE = 'LOBBY_GAMES_RESPONSE',
  GAME_REMOVED = 'GAME_REMOVED',
  SAVE_DECK = 'SAVE_DECK',
  SAVE_DECK_RESPONSE = 'SAVE_DECK_RESPONSE'
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
  icon: string;
}

export const TURN_STEPS: TurnStepInfo[] = [
  { step: TurnStep.UNTAP, displayName: 'Untap', phaseName: 'Beginning Phase', icon: 'UN' },
  { step: TurnStep.UPKEEP, displayName: 'Upkeep', phaseName: 'Beginning Phase', icon: 'UP' },
  { step: TurnStep.DRAW, displayName: 'Draw', phaseName: 'Beginning Phase', icon: 'DR' },
  { step: TurnStep.PRECOMBAT_MAIN, displayName: 'Precombat Main', phaseName: 'Precombat Main Phase', icon: 'M1' },
  { step: TurnStep.BEGINNING_OF_COMBAT, displayName: 'Beginning of Combat', phaseName: 'Combat Phase', icon: 'BC' },
  { step: TurnStep.DECLARE_ATTACKERS, displayName: 'Declare Attackers', phaseName: 'Combat Phase', icon: 'AT' },
  { step: TurnStep.DECLARE_BLOCKERS, displayName: 'Declare Blockers', phaseName: 'Combat Phase', icon: 'BL' },
  { step: TurnStep.COMBAT_DAMAGE, displayName: 'Combat Damage', phaseName: 'Combat Phase', icon: 'DM' },
  { step: TurnStep.END_OF_COMBAT, displayName: 'End of Combat', phaseName: 'Combat Phase', icon: 'EC' },
  { step: TurnStep.POSTCOMBAT_MAIN, displayName: 'Postcombat Main', phaseName: 'Postcombat Main Phase', icon: 'M2' },
  { step: TurnStep.END_STEP, displayName: 'End Step', phaseName: 'Ending Phase', icon: 'ES' },
  { step: TurnStep.CLEANUP, displayName: 'Cleanup', phaseName: 'Ending Phase', icon: 'CL' },
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
  manaCost: string | null;
  loyaltyCost: number | null;
  minTargets: number;
  maxTargets: number;
  isManaAbility: boolean;
  variableLoyaltyCost: boolean;
}

export interface Card {
  id: string | null;
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
  colors: string[];
  needsTarget: boolean;
  needsSpellTarget: boolean;
  activatedAbilities: ActivatedAbilityView[];
  loyalty: number | null;
  hasConvoke: boolean;
  hasPhyrexianMana: boolean;
  phyrexianManaCount: number;
  token: boolean;
  watermark: string | null;
  hasAlternateCastingCost: boolean;
  alternateCostLifePayment: number;
  alternateCostSacrificeCount: number;
  alternateCostTapCount: number;
  alternateCostReturnCount: number;
  alternateCostManaCost: string | null;
  graveyardActivatedAbilities: ActivatedAbilityView[];
  handActivatedAbilities?: ActivatedAbilityView[];
  transformable: boolean;
  kickerCost: string | null;
  modalChoicesRequired: number;
  modalChoicesMax: number;
  modalOptional: boolean;
  modalOptions: ModalOptionView[] | null;
}

export interface ModalOptionView {
  label: string;
  needsTarget: boolean;
  needsSpellTarget: boolean;
  targetCount: number;
}

/** One attributed contribution of a continuous effect to a permanent's characteristics —
 * the per-source hover breakdown. Display-only; the aggregates on Permanent stay authoritative. */
export interface ModifierLine {
  source: string;
  power: number;
  toughness: number;
  /** Non-null when this source SETS base power; base lines fold in list order, last non-null wins. */
  basePower: number | null;
  baseToughness: number | null;
  gainedKeywords: string[];
  removedKeywords: string[];
  losesAllAbilities: boolean;
  /** Sublayer 7d P/T switch — two switches cancel, only the parity of switch lines matters. */
  switchesPt: boolean;
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
  removedKeywords: string[];
  effectivePower: number;
  effectiveToughness: number;
  chosenColor: string | null;
  chosenName: string | null;
  regenerationShield: number;
  attachedTo: string | null;
  cantBeBlocked: boolean;
  animatedCreature: boolean;
  /** All counters on this permanent, keyed by CounterType name (e.g. "LOYALTY", "CHARGE"). Only present counters are included. */
  counters: { [counterType: string]: number };
  attackTargetId: string | null;
  markedDamage: number;
  transformed: boolean;
  /** Secrets of Strixhaven "Prepared": true while this permanent is prepared (a castable copy of its
   * prepare spell sits in exile). Not a transform — the front face stays; the prepare spell is shown inset. */
  prepared: boolean;
  /** Per-source attribution of the continuous effects modifying this permanent (hover breakdown).
   * Optional: absent in hand-built mock data (e.g. the tutorial). */
  modifierLines?: ModifierLine[];
  /** Face-up cards imprinted on / exiled with this permanent (Mimic Vat, Oblivion Ring, ...),
   * shown tucked under it. Optional: absent in hand-built mock data. */
  exiledWithCards?: Card[];
  /** Cards exiled face down with this permanent (hideaway, Grimoire Thief, ...);
   * rendered as card backs so hidden information stays hidden. Zero when this is the
   * viewer's own permanent — the server sends faceDownExiledCards instead. */
  faceDownExiledCount?: number;
  /** The face-down exiled cards themselves — only present in the controller's copy of the
   * view; opponents receive faceDownExiledCount card backs instead. */
  faceDownExiledCards?: Card[];
}

export interface StackEntry {
  entryType: string;
  card: Card;
  controllerId: string;
  description: string;
  cardId: string;
  isSpell: boolean;
  targetId: string | null;
}

export interface GameLogSegment {
  type: 'text' | 'card';
  text?: string | null;
  card?: Card | null;
}

export interface GameLogEntry {
  segments: GameLogSegment[];
}

export function logText(text: string): GameLogEntry {
  return { segments: [{ type: 'text', text }] };
}

export interface Game {
  id: string;
  gameName: string;
  status: GameStatus;
  playerNames: string[];
  playerIds: string[];
  gameLog: GameLogEntry[];
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
  poisonCounters: number[];
  stack: StackEntry[];
  graveyards: Card[][];
  revealedLibraryTopCards: Card[][];
  mindControlledPlayerId?: string | null;
}

export interface LobbyGame {
  id: string;
  gameName: string;
  createdByUsername: string;
  playerCount: number;
  status: GameStatus;
  allRandom: boolean;
}

export interface DeckInfo {
  id: string;
  name: string;
}

export interface SetInfo {
  code: string;
  name: string;
  randomEligible: boolean;
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

export interface RegisterResponse {
  type: MessageType;
  message: string;
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

export interface LobbyGamesNotification {
  type: MessageType;
  games: LobbyGame[];
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
  poisonCounters: number[];
  hand: Card[];
  opponentHand: Card[];
  mulliganCount: number;
  manaPool: Record<string, number>;
  autoStopSteps: string[];
  playableCardIndices: number[];
  potentialPlayableCardIndices: number[];
  potentialManaTotal: number;
  potentialPayableAbilityIndices: Record<string, number[]>;
  playableGraveyardLandIndices: number[];
  playableFlashbackIndices: number[];
  playableExileCards: Card[];
  playableLibraryTopCards: Card[];
  newLogEntries: GameLogEntry[];
  searchTaxCost: number;
  mindControlledPlayerId?: string | null;
  revealedLibraryTopCards: Card[][];
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

export interface AttackTarget {
  id: string;
  name: string;
  isPlayer: boolean;
}

export interface AvailableAttackersNotification {
  type: MessageType;
  attackerIndices: number[];
  mustAttackIndices: number[];
  availableTargets: AttackTarget[];
  taxPerCreature: number;
  mustAttackWithAtLeastOne: boolean;
}

export interface AvailableBlockersNotification {
  type: MessageType;
  blockerIndices: number[];
  attackerIndices: number[];
  legalBlockPairs: Record<number, number[]>;
  mustBeBlockedAttackerIndices: number[];
  menaceAttackerIndices: number[];
  mustBlockRequirements: Record<number, number[]>;
}

export interface GameOverNotification {
  type: MessageType;
  /** Null when the game ends in a draw (e.g. Triskaidekaphobia). */
  winnerId: string | null;
  winnerName: string | null;
}

export type InteractionShape =
  'CARD_INDEX_PICK' | 'GRAVEYARD_INDEX_PICK' | 'LIBRARY_INDEX_PICK' | 'PERMANENT_PICK' |
  'MULTI_CARD_PICK' | 'MULTI_PERMANENT_PICK' | 'LIST_PICK' | 'ACCEPT_DECLINE' |
  'NUMBER_PICK' | 'SCRY_ORDER' | 'CARD_ORDER' | 'HAND_TOP_BOTTOM';

// The single prompt message for every pending interaction. The shape selects the input UI
// and the answer payload; the optional fields carry the shape's data (unused fields are null).
export interface InteractionPromptNotification {
  type: MessageType;
  shape: InteractionShape;
  prompt: string;
  cardIndices?: number[];
  cards?: Card[];
  cardIds?: string[];
  permanentIds?: string[];
  playerIds?: string[];
  options?: string[];
  maxCount?: number;
  declinable?: boolean;
  canPay?: boolean;
  manaCost?: string | null;
  cardName?: string;
  allGraveyards?: boolean;
  searchable?: boolean;
}

export interface RevealHandNotification {
  type: MessageType;
  cards: Card[];
  playerName: string;
}

export interface RevealLibraryTopNotification {
  type: MessageType;
  cards: Card[];
  playerName: string;
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
  isDeathtouch: boolean;
  singleRecipient: boolean;
}

export interface BrowseCardInfo {
  name: string;
  collectorNumber: string;
  setCode: string;
  manaCost: string | null;
  typeLine: string;
  rarity: string;
  power: string | null;
  toughness: string | null;
  color: string | null;
  colors: string[];
  implemented: boolean;
  cardText: string | null;
  keywords: string[];
  type: string;
  additionalTypes: string[];
  supertypes: string[];
  subtypes: string[];
  loyalty: number | null;
  backFace: BrowseCardInfo | null;
}

export interface CardListResponse {
  type: MessageType;
  setCode: string;
  cards: BrowseCardInfo[];
}

export interface SaveDeckResponse {
  type: MessageType;
  deck: DeckInfo;
}

export interface ValidTargetsResponse {
  type: MessageType;
  validPermanentIds: string[];
  validPlayerIds: string[];
  validGraveyardCardIds: string[];
  minTargets: number;
  maxTargets: number;
  prompt: string;
}

export type WebSocketMessage = LoginResponse | GameNotification | LobbyGameNotification | GameStateNotification | MulliganResolvedNotification | SelectCardsToBottomNotification | AvailableAttackersNotification | AvailableBlockersNotification | GameOverNotification | InteractionPromptNotification | RevealHandNotification | RevealLibraryTopNotification | DraftJoinedNotification | DraftPackUpdateNotification | DeckBuildingStateNotification | TournamentUpdateNotification | TournamentGameReadyNotification | DraftFinishedNotification | CombatDamageAssignmentNotification | CardListResponse | ValidTargetsResponse | SaveDeckResponse;

export interface User {
  userId: string;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private readonly WS_URL = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/login`;
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
  pendingGameInputMessage: WebSocketMessage | null = null;

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

          // Buffer game input messages so they're available when game component mounts on rejoin
          if (message.type === MessageType.AVAILABLE_ATTACKERS ||
              message.type === MessageType.AVAILABLE_BLOCKERS ||
              message.type === MessageType.INTERACTION_PROMPT ||
              message.type === MessageType.COMBAT_DAMAGE_ASSIGNMENT) {
            this.pendingGameInputMessage = message;
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

  register(username: string, password: string, confirmPassword: string): Observable<RegisterResponse> {
    return new Observable(observer => {
      // One-shot socket: the server answers the REGISTER message and closes
      const ws = new WebSocket(this.WS_URL);

      ws.onopen = () => {
        ws.send(JSON.stringify({
          type: MessageType.REGISTER,
          username: username,
          password: password,
          confirmPassword: confirmPassword
        }));
      };

      ws.onmessage = (event) => {
        try {
          const response: RegisterResponse = JSON.parse(event.data);
          observer.next(response);
          observer.complete();
        } catch (error) {
          observer.error('Failed to parse response');
        }
        ws.close();
      };

      ws.onerror = () => {
        if (!observer.closed) {
          observer.error('WebSocket connection error');
        }
      };

      ws.onclose = () => {
        if (!observer.closed) {
          observer.complete();
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
    this.pendingGameInputMessage = null;
  }
}
