import { computed, inject, Injectable, Signal, signal } from '@angular/core';
import {
  WebsocketService, Game, MessageType, Card, Permanent,
  InteractionPromptNotification, RevealHandNotification, RevealLibraryTopNotification,
  CombatDamageAssignmentNotification, ValidTargetsResponse
} from './websocket.service';
import { TargetingChoiceService } from './targeting-choice.service';
import { LibraryChoiceService } from './library-choice.service';
import { DamageChoiceService } from './damage-choice.service';
import { isPermanentCreature } from '../components/game/battlefield.utils';

@Injectable({ providedIn: 'root' })
export class GameChoiceService {

  constructor(private websocketService: WebsocketService) {}

  readonly targeting = inject(TargetingChoiceService);
  readonly library = inject(LibraryChoiceService);
  readonly damage = inject(DamageChoiceService);

  private gameSignal!: Signal<Game | null>;

  init(
    gameSignal: Signal<Game | null>,
    myBattlefieldFn: () => Permanent[],
    opponentBattlefieldFn: () => Permanent[],
    totalManaFn: () => number,
    isStrictlyPlayableFn: (index: number) => boolean = () => false,
    potentialTotalManaFn: () => number = () => 0
  ): void {
    this.reset();
    this.gameSignal = gameSignal;
    this.targeting.init(gameSignal, myBattlefieldFn, opponentBattlefieldFn, totalManaFn,
      isStrictlyPlayableFn, potentialTotalManaFn);
  }

  reset(): void {
    // Hand choice
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
    this.handChoiceCanDecline = false;
    // List choice
    this.choosingFromList = false;
    this.listChoices.set([]);
    this.listChoicePrompt = '';
    this.listChoiceSearchable = false;
    this.listChoiceSearchQuery.set('');
    // May ability
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
    this.mayAbilityCanPay = true;
    this.mayAbilityManaCost = null;
    // Permanent choice
    this.choosingPermanent = false;
    this.choosablePermanentIds.set(new Set());
    this.choosablePlayerIds.set(new Set());
    this.permanentChoicePrompt = '';
    // Multi-permanent
    this.choosingMultiplePermanents = false;
    this.multiPermanentChoiceIds.set(new Set());
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = 0;
    this.multiPermanentChoicePrompt = '';
    // Multi-graveyard
    this.choosingGraveyardCards = false;
    this.multiGraveyardCards = [];
    this.graveyardChoiceCardIds = [];
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = 0;
    this.multiGraveyardPrompt = '';
    // Reveal hand
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandPlayerName = '';
    // Reveal library top
    this.revealingLibraryTop = false;
    this.revealedLibraryTopCards = [];
    this.revealedLibraryTopPlayerName = '';
    // Choose from revealed hand
    this.choosingFromRevealedHand = false;
    this.revealedHandChoosableIndices = new Set();
    this.revealedHandChoicePrompt = '';
    this.revealedHandChoiceOptional = false;
    // Choose from graveyard
    this.choosingFromGraveyard = false;
    this.graveyardChoiceIndices = [];
    this.graveyardChoicePrompt = '';
    this.graveyardChoiceAllGraveyards = false;
    // X value choice
    this.awaitingXValueChoice = false;
    this.xValueChoicePrompt = '';
    this.xValueChoiceMaxValue = 0;
    this.xValueChoiceInput = 0;
    // Sub-services
    this.targeting.reset();
    this.library.reset();
    this.damage.reset();
  }

  // --- Hand choice state ---
  choosingFromHand = false;
  choosableHandIndices = signal(new Set<number>());
  handChoicePrompt = '';
  handChoiceCanDecline = false;

  // --- List choice state ---
  choosingFromList = false;
  listChoices = signal<string[]>([]);
  listChoicePrompt = '';
  // When true the options are a large card-name list; render an autocomplete search box.
  listChoiceSearchable = false;
  listChoiceSearchQuery = signal('');

  // Options shown under the search box: filtered by the query (case-insensitive substring),
  // capped so a full deck's worth of names never floods the panel. Empty until the player types.
  filteredListChoices = computed<string[]>(() => {
    const query = this.listChoiceSearchQuery().trim().toLowerCase();
    if (!query) return [];
    return this.listChoices()
      .filter(option => option.toLowerCase().includes(query))
      .slice(0, 50);
  });

  // --- May ability state ---
  awaitingMayAbility = false;
  mayAbilityPrompt = '';
  mayAbilityCanPay = true;
  mayAbilityManaCost: string | null = null;

  // --- Permanent choice state ---
  choosingPermanent = false;
  choosablePermanentIds = signal(new Set<string>());
  choosablePlayerIds = signal(new Set<string>());
  permanentChoicePrompt = '';

  // --- Multi-permanent state ---
  choosingMultiplePermanents = false;
  multiPermanentChoiceIds = signal(new Set<string>());
  multiPermanentSelectedIds = signal(new Set<string>());
  multiPermanentMaxCount = 0;
  multiPermanentChoicePrompt = '';

  // --- Multi-graveyard choice state ---
  choosingGraveyardCards = false;
  multiGraveyardCards: Card[] = [];
  graveyardChoiceCardIds: string[] = [];
  graveyardChoiceSelectedIds = signal(new Set<string>());
  graveyardChoiceMaxCount = 0;
  multiGraveyardPrompt = '';

  // --- Reveal hand state ---
  revealingHand = false;
  revealedHandCards: Card[] = [];
  revealedHandPlayerName = '';

  // --- Reveal library top state (Orcish Spy) ---
  revealingLibraryTop = false;
  revealedLibraryTopCards: Card[] = [];
  revealedLibraryTopPlayerName = '';

  // --- Choose from revealed hand state ---
  choosingFromRevealedHand = false;
  revealedHandChoosableIndices = new Set<number>();
  revealedHandChoicePrompt = '';
  revealedHandChoiceOptional = false;

  // --- Choose from graveyard state ---
  choosingFromGraveyard = false;
  graveyardChoiceIndices: number[] = [];
  graveyardChoicePrompt = '';
  graveyardChoiceAllGraveyards = false;

  // --- X value choice state ---
  awaitingXValueChoice = false;
  xValueChoicePrompt = '';
  xValueChoiceMaxValue = 0;
  xValueChoiceInput = 0;

  // ========== Message handlers ==========

  /**
   * The single entry point for interaction prompts: routes by the message's shape to the
   * same UI state each per-kind message used to set. CARD_INDEX_PICK distinguishes picking
   * from the player's own hand (no cards payload) from picking out of presented cards
   * (a revealed hand).
   */
  handleInteractionPrompt(msg: InteractionPromptNotification): void {
    switch (msg.shape) {
      case 'CARD_INDEX_PICK':
        if (msg.cards) {
          this.revealingHand = true;
          this.choosingFromRevealedHand = true;
          this.revealedHandCards = msg.cards;
          this.revealedHandChoosableIndices = new Set(msg.cardIndices ?? []);
          this.revealedHandChoicePrompt = msg.prompt;
          this.revealedHandChoiceOptional = msg.declinable ?? false;
          this.revealedHandPlayerName = '';
        } else {
          this.choosingFromHand = true;
          this.choosableHandIndices.set(new Set(msg.cardIndices ?? []));
          this.handChoicePrompt = msg.prompt;
          this.handChoiceCanDecline = msg.declinable ?? false;
        }
        break;
      case 'LIST_PICK':
        this.choosingFromList = true;
        this.listChoices.set(msg.options ?? []);
        this.listChoicePrompt = msg.prompt;
        this.listChoiceSearchable = msg.searchable ?? false;
        this.listChoiceSearchQuery.set('');
        break;
      case 'ACCEPT_DECLINE':
        this.awaitingMayAbility = true;
        this.mayAbilityPrompt = msg.prompt;
        this.mayAbilityCanPay = msg.canPay ?? true;
        this.mayAbilityManaCost = msg.manaCost ?? null;
        break;
      case 'PERMANENT_PICK':
        this.choosingPermanent = true;
        this.choosablePermanentIds.set(new Set(msg.permanentIds ?? []));
        this.choosablePlayerIds.set(new Set(msg.playerIds ?? []));
        this.permanentChoicePrompt = msg.prompt;
        break;
      case 'MULTI_PERMANENT_PICK':
        this.choosingMultiplePermanents = true;
        this.multiPermanentChoiceIds.set(new Set(msg.permanentIds ?? []));
        this.multiPermanentSelectedIds.set(new Set());
        this.multiPermanentMaxCount = msg.maxCount ?? 0;
        this.multiPermanentChoicePrompt = msg.prompt;
        break;
      case 'MULTI_CARD_PICK':
        this.choosingGraveyardCards = true;
        this.multiGraveyardCards = msg.cards ?? [];
        this.graveyardChoiceCardIds = msg.cardIds ?? [];
        this.graveyardChoiceSelectedIds.set(new Set());
        this.graveyardChoiceMaxCount = msg.maxCount ?? 0;
        this.multiGraveyardPrompt = msg.prompt;
        break;
      case 'GRAVEYARD_INDEX_PICK':
        this.choosingFromGraveyard = true;
        this.graveyardChoiceIndices = msg.cardIndices ?? [];
        this.graveyardChoicePrompt = msg.prompt;
        this.graveyardChoiceAllGraveyards = msg.allGraveyards ?? false;
        break;
      case 'NUMBER_PICK':
        this.awaitingXValueChoice = true;
        this.xValueChoicePrompt = msg.prompt;
        this.xValueChoiceMaxValue = msg.maxCount ?? 0;
        this.xValueChoiceInput = msg.maxCount ?? 0;
        break;
      case 'SCRY_ORDER':
        this.library.handleScry(msg);
        break;
      case 'CARD_ORDER':
        this.library.handleReorderLibraryCards(msg);
        break;
      case 'LIBRARY_INDEX_PICK':
        this.library.handleChooseCardFromLibrary(msg);
        break;
      case 'HAND_TOP_BOTTOM':
        this.library.handleChooseHandTopBottom(msg);
        break;
    }
  }

  handleRevealHand(msg: RevealHandNotification): void {
    this.revealingHand = true;
    this.revealedHandCards = msg.cards;
    this.revealedHandPlayerName = msg.playerName;
  }

  handleRevealLibraryTop(msg: RevealLibraryTopNotification): void {
    this.revealingLibraryTop = true;
    this.revealedLibraryTopCards = msg.cards;
    this.revealedLibraryTopPlayerName = msg.playerName;
  }

  handleCombatDamageAssignment(msg: CombatDamageAssignmentNotification): void {
    this.damage.handleCombatDamageAssignment(msg);
  }

  handleValidTargetsResponse(msg: ValidTargetsResponse): void {
    this.targeting.handleValidTargetsResponse(msg);
  }

  // ========== User actions ==========

  chooseCardFromHand(index: number): void {
    const g = this.gameSignal();
    if (!g || !this.choosingFromHand) return;
    if (!this.choosableHandIndices().has(index)) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'CARD_INDEX_PICK',
      index: index
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  declineHandChoice(): void {
    const g = this.gameSignal();
    if (!g || !this.choosingFromHand) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'CARD_INDEX_PICK',
      index: -1
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  updateListChoiceSearch(query: string): void {
    this.listChoiceSearchQuery.set(query);
  }

  chooseFromList(choice: string): void {
    if (!this.choosingFromList) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'LIST_PICK',
      choice: choice
    });
    this.choosingFromList = false;
    this.listChoices.set([]);
    this.listChoicePrompt = '';
    this.listChoiceSearchable = false;
    this.listChoiceSearchQuery.set('');
  }

  acceptMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'ACCEPT_DECLINE',
      accepted: true
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
    this.mayAbilityCanPay = true;
    this.mayAbilityManaCost = null;
  }

  declineMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'ACCEPT_DECLINE',
      accepted: false
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
    this.mayAbilityCanPay = true;
    this.mayAbilityManaCost = null;
  }

  confirmXValueChoice(): void {
    if (!this.awaitingXValueChoice) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'NUMBER_PICK',
      number: this.xValueChoiceInput
    });
    this.awaitingXValueChoice = false;
    this.xValueChoicePrompt = '';
    this.xValueChoiceMaxValue = 0;
    this.xValueChoiceInput = 0;
  }

  cancelXValueChoice(): void {
    if (!this.awaitingXValueChoice) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'NUMBER_PICK',
      number: 0
    });
    this.awaitingXValueChoice = false;
    this.xValueChoicePrompt = '';
    this.xValueChoiceMaxValue = 0;
    this.xValueChoiceInput = 0;
  }

  choosePermanent(permanentId: string): void {
    if (!this.choosingPermanent) return;
    if (!this.choosablePermanentIds().has(permanentId) && !this.choosablePlayerIds().has(permanentId)) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'PERMANENT_PICK',
      id: permanentId
    });
    this.choosingPermanent = false;
    this.choosablePermanentIds.set(new Set());
    this.choosablePlayerIds.set(new Set());
    this.permanentChoicePrompt = '';
  }

  toggleMultiPermanentSelection(permanentId: string): void {
    if (!this.choosingMultiplePermanents) return;
    if (!this.multiPermanentChoiceIds().has(permanentId)) return;
    const selected = new Set(this.multiPermanentSelectedIds());
    if (selected.has(permanentId)) {
      selected.delete(permanentId);
    } else if (selected.size < this.multiPermanentMaxCount) {
      selected.add(permanentId);
    }
    this.multiPermanentSelectedIds.set(selected);
  }

  confirmMultiPermanentChoice(): void {
    if (!this.choosingMultiplePermanents) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'MULTI_PERMANENT_PICK',
      ids: Array.from(this.multiPermanentSelectedIds())
    });
    this.choosingMultiplePermanents = false;
    this.multiPermanentChoiceIds.set(new Set());
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = 0;
    this.multiPermanentChoicePrompt = '';
  }

  toggleGraveyardCardSelection(index: number): void {
    if (!this.choosingGraveyardCards) return;
    const cardId = this.graveyardChoiceCardIds[index];
    if (!cardId) return;
    const selected = new Set(this.graveyardChoiceSelectedIds());
    if (selected.has(cardId)) {
      selected.delete(cardId);
    } else if (selected.size < this.graveyardChoiceMaxCount) {
      selected.add(cardId);
    }
    this.graveyardChoiceSelectedIds.set(selected);
  }

  confirmGraveyardCardChoice(): void {
    if (!this.choosingGraveyardCards) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'MULTI_CARD_PICK',
      ids: Array.from(this.graveyardChoiceSelectedIds())
    });
    this.choosingGraveyardCards = false;
    this.multiGraveyardCards = [];
    this.graveyardChoiceCardIds = [];
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = 0;
    this.multiGraveyardPrompt = '';
  }

  getOptionDisplayName(color: string): string {
    switch (color) {
      case 'WHITE': return 'White';
      case 'BLUE': return 'Blue';
      case 'BLACK': return 'Black';
      case 'RED': return 'Red';
      case 'GREEN': return 'Green';
      case 'PLAINS': return 'Plains';
      case 'ISLAND': return 'Island';
      case 'SWAMP': return 'Swamp';
      case 'MOUNTAIN': return 'Mountain';
      case 'FOREST': return 'Forest';
      case 'ARTIFACT': return 'Artifact';
      case 'CREATURE': return 'Creature';
      case 'ENCHANTMENT': return 'Enchantment';
      case 'LAND': return 'Land';
      case 'PLANESWALKER': return 'Planeswalker';
      case 'ODD': return 'Odd';
      case 'EVEN': return 'Even';
      default: return color;
    }
  }

  isChoosableCard(index: number): boolean {
    return this.choosableHandIndices().has(index);
  }

  // ========== Reveal hand ==========

  closeRevealHand(): void {
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandPlayerName = '';
  }

  closeRevealLibraryTop(): void {
    this.revealingLibraryTop = false;
    this.revealedLibraryTopCards = [];
    this.revealedLibraryTopPlayerName = '';
  }

  chooseFromRevealedHand(index: number): void {
    if (!this.choosingFromRevealedHand) return;
    if (!this.revealedHandChoosableIndices.has(index)) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'CARD_INDEX_PICK',
      index: index
    });
    this.closeRevealedHandChoice();
  }

  declineFromRevealedHand(): void {
    if (!this.choosingFromRevealedHand || !this.revealedHandChoiceOptional) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'CARD_INDEX_PICK',
      index: -1
    });
    this.closeRevealedHandChoice();
  }

  private closeRevealedHandChoice(): void {
    this.choosingFromRevealedHand = false;
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandChoosableIndices = new Set();
    this.revealedHandChoicePrompt = '';
    this.revealedHandChoiceOptional = false;
  }

  isRevealedHandCardChoosable(index: number): boolean {
    return this.choosingFromRevealedHand && this.revealedHandChoosableIndices.has(index);
  }

  // ========== Mana tapping (X value choice & may ability) ==========

  canTapForMana(perm: Permanent): boolean {
    const allowTap = this.awaitingXValueChoice || (this.awaitingMayAbility && this.mayAbilityManaCost != null);
    if (!allowTap || perm.tapped) return false;
    if (perm.summoningSick && isPermanentCreature(perm)) return false;
    // Has ON_TAP mana effects (lands, mana rocks)
    if (perm.card.hasTapAbility) return true;
    // Has mana-producing activated ability (e.g., Birds of Paradise)
    return perm.card.activatedAbilities.some(a => a.isManaAbility);
  }

  updateMayAbilityCanPay(manaPool: Record<string, number>): void {
    if (!this.awaitingMayAbility || !this.mayAbilityManaCost) {
      return;
    }
    const tokens = this.mayAbilityManaCost.match(/\{([^}]+)\}/g)?.map(t => t.slice(1, -1)) ?? [];
    const coloredNeeded: Record<string, number> = {};
    let genericNeeded = 0;
    let hasX = false;

    for (const token of tokens) {
      if (/^\d+$/.test(token)) {
        genericNeeded += parseInt(token);
      } else if (token === 'X') {
        hasX = true;
      } else {
        coloredNeeded[token] = (coloredNeeded[token] ?? 0) + 1;
      }
    }

    // Check each colored requirement
    for (const [color, needed] of Object.entries(coloredNeeded)) {
      if ((manaPool[color] ?? 0) < needed) {
        this.mayAbilityCanPay = false;
        return;
      }
    }

    // Calculate remaining mana after colored costs
    const totalAvailable = Object.values(manaPool).reduce((sum, v) => sum + v, 0);
    const coloredSpent = Object.values(coloredNeeded).reduce((sum, v) => sum + v, 0);
    const remaining = totalAvailable - coloredSpent;

    this.mayAbilityCanPay = hasX ? remaining > genericNeeded : remaining >= genericNeeded;
  }

  // ========== Graveyard choice ==========

  get graveyardChoiceCards(): { card: Card; index: number; owner: string }[] {
    const g = this.gameSignal();
    if (!g) return [];
    const validIndices = new Set(this.graveyardChoiceIndices);
    const result: { card: Card; index: number; owner: string }[] = [];

    if (this.graveyardChoiceAllGraveyards) {
      let poolIndex = 0;
      for (let playerIdx = 0; playerIdx < g.graveyards.length; playerIdx++) {
        const graveyard = g.graveyards[playerIdx];
        const ownerName = g.playerNames[playerIdx] ?? 'Unknown';
        for (const card of graveyard) {
          if (card.type === 'CREATURE' || card.type === 'ARTIFACT') {
            if (validIndices.has(poolIndex)) {
              result.push({ card, index: poolIndex, owner: ownerName });
            }
            poolIndex++;
          }
        }
      }
    } else {
      const myId = this.websocketService.currentUser?.userId ?? '';
      const myPlayerIdx = g.playerIds.indexOf(myId);
      const graveyard = g.graveyards[myPlayerIdx] ?? [];
      const ownerName = g.playerNames[myPlayerIdx] ?? 'Unknown';
      for (const idx of this.graveyardChoiceIndices) {
        if (idx >= 0 && idx < graveyard.length) {
          result.push({ card: graveyard[idx], index: idx, owner: ownerName });
        }
      }
    }

    return result;
  }

  chooseGraveyardCard(index: number): void {
    if (!this.choosingFromGraveyard) return;
    this.websocketService.send({
      type: MessageType.INTERACTION_ANSWER,
      shape: 'GRAVEYARD_INDEX_PICK',
      index: index
    });
    this.choosingFromGraveyard = false;
    this.graveyardChoiceIndices = [];
    this.graveyardChoicePrompt = '';
    this.graveyardChoiceAllGraveyards = false;
  }
}
