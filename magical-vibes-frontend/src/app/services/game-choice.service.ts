import { Injectable, Signal, signal } from '@angular/core';
import {
  WebsocketService, Game, MessageType, Card, Permanent, StackEntry, ActivatedAbilityView,
  ChooseCardFromHandNotification, ChooseColorNotification, MayAbilityNotification,
  ChoosePermanentNotification, ChooseMultiplePermanentsNotification,
  ChooseMultipleCardsFromGraveyardsNotification, ReorderLibraryCardsNotification,
  ChooseCardFromLibraryNotification, RevealHandNotification,
  ChooseFromRevealedHandNotification, ChooseCardFromGraveyardNotification,
  ChooseHandTopBottomNotification
} from './websocket.service';
import { isPermanentCreature } from '../components/game/battlefield.utils';

@Injectable({ providedIn: 'root' })
export class GameChoiceService {

  constructor(private websocketService: WebsocketService) {}

  private gameSignal!: Signal<Game | null>;
  private myBattlefieldFn!: () => Permanent[];
  private opponentBattlefieldFn!: () => Permanent[];
  private totalManaFn!: () => number;

  init(
    gameSignal: Signal<Game | null>,
    myBattlefieldFn: () => Permanent[],
    opponentBattlefieldFn: () => Permanent[],
    totalManaFn: () => number
  ): void {
    this.gameSignal = gameSignal;
    this.myBattlefieldFn = myBattlefieldFn;
    this.opponentBattlefieldFn = opponentBattlefieldFn;
    this.totalManaFn = totalManaFn;
  }

  private get hasPriority(): boolean {
    const g = this.gameSignal();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
  }

  // --- Hand choice state ---
  choosingFromHand = false;
  choosableHandIndices = signal(new Set<number>());
  handChoicePrompt = '';

  // --- Color choice state ---
  choosingColor = false;
  colorChoices: string[] = [];
  colorChoicePrompt = '';

  // --- May ability state ---
  awaitingMayAbility = false;
  mayAbilityPrompt = '';

  // --- Permanent choice state ---
  choosingPermanent = false;
  choosablePermanentIds = signal(new Set<string>());
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

  // --- Ability picker state ---
  choosingAbility = false;
  abilityChoicePermanentIndex = -1;
  abilityChoices: { ability: ActivatedAbilityView; index: number; usable: boolean }[] = [];

  // --- X cost prompt state ---
  choosingXValue = false;
  xValueCardIndex = -1;
  xValueCardName = '';
  xValueInput = 0;
  xValueMaximum = 0;

  // --- Targeting state (for instants and activated abilities) ---
  targeting = false;
  targetingCardIndex = -1;
  targetingCardName = '';
  targetingForAbility = false;
  targetingForPlayer = false;
  targetingAllowedTypes: string[] = [];
  targetingAllowedColors: string[] = [];
  targetingRequiresAttacking = false;
  targetingAbilityIndex = -1;
  pendingAbilityXValue: number | null = null;

  // --- Spell targeting state (for counterspells) ---
  targetingSpell = false;
  targetingSpellCardIndex = -1;
  targetingSpellCardName = '';

  // --- Library search state ---
  searchingLibrary = false;
  librarySearchCards: Card[] = [];
  librarySearchPrompt = '';
  librarySearchCanFailToFind = false;

  // --- Library reorder state ---
  reorderingLibrary = false;
  reorderAllCards: Card[] = [];
  reorderAvailableIndices: number[] = [];
  reorderOriginalIndices: number[] = [];
  reorderPrompt = '';

  // --- Telling Time state ---
  choosingHandTopBottom = false;
  handTopBottomCards: Card[] = [];
  handTopBottomHandIndex: number | null = null;
  handTopBottomTopIndex: number | null = null;

  // --- Reveal hand state ---
  revealingHand = false;
  revealedHandCards: Card[] = [];
  revealedHandPlayerName = '';

  // --- Choose from revealed hand state ---
  choosingFromRevealedHand = false;
  revealedHandChoosableIndices = new Set<number>();
  revealedHandChoicePrompt = '';

  // --- Choose from graveyard state ---
  choosingFromGraveyard = false;
  graveyardChoiceIndices: number[] = [];
  graveyardChoicePrompt = '';

  // --- Multi-target state (for spells like "one or two target creatures") ---
  multiTargeting = false;
  multiTargetCardIndex = -1;
  multiTargetCardName = '';
  multiTargetMinCount = 0;
  multiTargetMaxCount = 0;
  multiTargetSelectedIds = signal<string[]>([]);

  // --- Convoke state ---
  convoking = false;
  convokeCardIndex = -1;
  convokeCardName = '';
  convokeSelectedCreatureIds = signal<string[]>([]);
  private pendingMultiTargetIds: string[] = [];
  private pendingConvokeCard: Card | null = null;

  // --- Damage distribution state ---
  distributingDamage = false;
  damageDistributionCardIndex = -1;
  damageDistributionCardName = '';
  damageDistributionXValue = 0;
  damageAssignments: Map<string, number> = new Map();

  // ========== Message handlers ==========

  handleChooseCardFromHand(msg: ChooseCardFromHandNotification): void {
    this.choosingFromHand = true;
    this.choosableHandIndices.set(new Set(msg.cardIndices));
    this.handChoicePrompt = msg.prompt;
  }

  handleChooseColor(msg: ChooseColorNotification): void {
    this.choosingColor = true;
    this.colorChoices = msg.colors;
    this.colorChoicePrompt = msg.prompt;
  }

  handleMayAbilityChoice(msg: MayAbilityNotification): void {
    this.awaitingMayAbility = true;
    this.mayAbilityPrompt = msg.prompt;
  }

  handleChoosePermanent(msg: ChoosePermanentNotification): void {
    this.choosingPermanent = true;
    this.choosablePermanentIds.set(new Set(msg.permanentIds));
    this.permanentChoicePrompt = msg.prompt;
  }

  handleChooseMultiplePermanents(msg: ChooseMultiplePermanentsNotification): void {
    this.choosingMultiplePermanents = true;
    this.multiPermanentChoiceIds.set(new Set(msg.permanentIds));
    this.multiPermanentSelectedIds.set(new Set());
    this.multiPermanentMaxCount = msg.maxCount;
    this.multiPermanentChoicePrompt = msg.prompt;
  }

  handleChooseMultipleCardsFromGraveyards(msg: ChooseMultipleCardsFromGraveyardsNotification): void {
    this.choosingGraveyardCards = true;
    this.multiGraveyardCards = msg.cards;
    this.graveyardChoiceCardIds = msg.cardIds;
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = msg.maxCount;
    this.multiGraveyardPrompt = msg.prompt;
  }

  handleReorderLibraryCards(msg: ReorderLibraryCardsNotification): void {
    this.reorderingLibrary = true;
    this.reorderAllCards = msg.cards;
    this.reorderAvailableIndices = msg.cards.map((_, i) => i);
    this.reorderOriginalIndices = [];
    this.reorderPrompt = msg.prompt;
  }

  handleChooseCardFromLibrary(msg: ChooseCardFromLibraryNotification): void {
    this.searchingLibrary = true;
    this.librarySearchCards = msg.cards;
    this.librarySearchPrompt = msg.prompt;
    this.librarySearchCanFailToFind = msg.canFailToFind;
  }

  handleChooseHandTopBottom(msg: ChooseHandTopBottomNotification): void {
    this.choosingHandTopBottom = true;
    this.handTopBottomCards = msg.cards;
    this.handTopBottomHandIndex = null;
    this.handTopBottomTopIndex = null;
  }

  handleRevealHand(msg: RevealHandNotification): void {
    this.revealingHand = true;
    this.revealedHandCards = msg.cards;
    this.revealedHandPlayerName = msg.playerName;
  }

  handleChooseFromRevealedHand(msg: ChooseFromRevealedHandNotification): void {
    this.revealingHand = true;
    this.choosingFromRevealedHand = true;
    this.revealedHandCards = msg.cards;
    this.revealedHandChoosableIndices = new Set(msg.validIndices);
    this.revealedHandChoicePrompt = msg.prompt;
    this.revealedHandPlayerName = '';
  }

  handleChooseCardFromGraveyard(msg: ChooseCardFromGraveyardNotification): void {
    this.choosingFromGraveyard = true;
    this.graveyardChoiceIndices = msg.cardIndices;
    this.graveyardChoicePrompt = msg.prompt;
  }

  // ========== User actions ==========

  chooseCardFromHand(index: number): void {
    const g = this.gameSignal();
    if (!g || !this.choosingFromHand) return;
    if (!this.choosableHandIndices().has(index)) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: index
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  declineHandChoice(): void {
    const g = this.gameSignal();
    if (!g || !this.choosingFromHand) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: -1
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  chooseColor(color: string): void {
    if (!this.choosingColor) return;
    this.websocketService.send({
      type: MessageType.COLOR_CHOSEN,
      color: color
    });
    this.choosingColor = false;
    this.colorChoices = [];
    this.colorChoicePrompt = '';
  }

  acceptMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.MAY_ABILITY_CHOSEN,
      accepted: true
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
  }

  declineMayAbility(): void {
    if (!this.awaitingMayAbility) return;
    this.websocketService.send({
      type: MessageType.MAY_ABILITY_CHOSEN,
      accepted: false
    });
    this.awaitingMayAbility = false;
    this.mayAbilityPrompt = '';
  }

  choosePermanent(permanentId: string): void {
    if (!this.choosingPermanent) return;
    if (!this.choosablePermanentIds().has(permanentId)) return;
    this.websocketService.send({
      type: MessageType.PERMANENT_CHOSEN,
      permanentId: permanentId
    });
    this.choosingPermanent = false;
    this.choosablePermanentIds.set(new Set());
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
      type: MessageType.MULTIPLE_PERMANENTS_CHOSEN,
      permanentIds: Array.from(this.multiPermanentSelectedIds())
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
      type: MessageType.MULTIPLE_GRAVEYARD_CARDS_CHOSEN,
      cardIds: Array.from(this.graveyardChoiceSelectedIds())
    });
    this.choosingGraveyardCards = false;
    this.multiGraveyardCards = [];
    this.graveyardChoiceCardIds = [];
    this.graveyardChoiceSelectedIds.set(new Set());
    this.graveyardChoiceMaxCount = 0;
    this.multiGraveyardPrompt = '';
  }

  getColorDisplayName(color: string): string {
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
      default: return color;
    }
  }

  isChoosableCard(index: number): boolean {
    return this.choosableHandIndices().has(index);
  }

  // ========== Play card / targeting / abilities ==========

  playCard(index: number, isCardPlayable: (i: number) => boolean): void {
    const g = this.gameSignal();
    if (g && isCardPlayable(index)) {
      const card = g.hand[index];
      const hasXCost = card.manaCost?.includes('{X}') ?? false;

      if (hasXCost) {
        this.choosingXValue = true;
        this.xValueCardIndex = index;
        this.xValueCardName = card.name;
        this.xValueInput = 0;
        this.xValueMaximum = this.totalManaFn() - 1; // subtract 1 for the {W} base cost
        return;
      }
      if (card.needsSpellTarget) {
        this.targetingSpell = true;
        this.targetingSpellCardIndex = index;
        this.targetingSpellCardName = card.name;
        return;
      }
      // Multi-target spells (e.g. "one or two target creatures")
      if (card.maxTargets > 1 && card.needsTarget) {
        this.multiTargeting = true;
        this.multiTargetCardIndex = index;
        this.multiTargetCardName = card.name;
        this.multiTargetMinCount = card.minTargets;
        this.multiTargetMaxCount = card.maxTargets;
        this.multiTargetSelectedIds.set([]);
        this.pendingConvokeCard = card;
        return;
      }
      if (card.needsTarget) {
        this.targeting = true;
        this.targetingCardIndex = index;
        this.targetingCardName = card.name;
        this.targetingForAbility = false;
        this.targetingForPlayer = card.targetsPlayer ?? false;
        this.targetingRequiresAttacking = card.requiresAttackingTarget ?? false;
        this.targetingAbilityIndex = -1;
        this.pendingAbilityXValue = null;
        this.targetingAllowedTypes = card.allowedTargetTypes?.length > 0 ? card.allowedTargetTypes : [];
        // If this single-target card has convoke, store it for later
        this.pendingConvokeCard = card.hasConvoke ? card : null;
        return;
      }
      // No targets needed — check for convoke
      if (card.hasConvoke) {
        this.pendingConvokeCard = card;
        this.pendingMultiTargetIds = [];
        this.enterConvokeMode(index, card);
        return;
      }
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetPermanentId: null });
    }
  }

  confirmXValue(): void {
    const g = this.gameSignal();
    if (!g) return;

    if (this.targetingForAbility) {
      const perm = this.myBattlefieldFn()[this.xValueCardIndex];
      const ability = perm?.card.activatedAbilities[this.targetingAbilityIndex];
      if (ability?.needsTarget) {
        // Store X value and enter targeting mode
        this.pendingAbilityXValue = this.xValueInput;
        this.choosingXValue = false;
        this.targeting = true;
        this.targetingCardIndex = this.xValueCardIndex;
        this.targetingCardName = this.xValueCardName;
        this.targetingForPlayer = ability.targetsPlayer;
        this.targetingAllowedTypes = ability.allowedTargetTypes ?? [];
        this.targetingAllowedColors = ability.allowedTargetColors ?? [];
        return;
      }
      // X value only, no target
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.xValueCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        xValue: this.xValueInput
      });
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.xValueCardIndex,
        xValue: this.xValueInput,
        targetPermanentId: null
      });
    }
    this.choosingXValue = false;
    this.xValueCardIndex = -1;
    this.xValueCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  cancelXValue(): void {
    this.choosingXValue = false;
    this.xValueCardIndex = -1;
    this.xValueCardName = '';
    this.xValueInput = 0;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  get damageDistributionRemaining(): number {
    let assigned = 0;
    this.damageAssignments.forEach(v => assigned += v);
    return this.damageDistributionXValue - assigned;
  }

  assignDamage(permanentId: string): void {
    if (!this.distributingDamage || this.damageDistributionRemaining <= 0) return;
    const current = this.damageAssignments.get(permanentId) ?? 0;
    this.damageAssignments.set(permanentId, current + 1);
  }

  unassignDamage(permanentId: string): void {
    if (!this.distributingDamage) return;
    const current = this.damageAssignments.get(permanentId) ?? 0;
    if (current <= 1) {
      this.damageAssignments.delete(permanentId);
    } else {
      this.damageAssignments.set(permanentId, current - 1);
    }
  }

  getDamageAssigned(permanentId: string): number {
    return this.damageAssignments.get(permanentId) ?? 0;
  }

  confirmDamageDistribution(): void {
    if (this.damageDistributionRemaining !== 0) return;
    const assignments: Record<string, number> = {};
    this.damageAssignments.forEach((v, k) => assignments[k] = v);
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.damageDistributionCardIndex,
      xValue: this.damageDistributionXValue,
      damageAssignments: assignments
    });
    this.cancelDamageDistribution();
  }

  cancelDamageDistribution(): void {
    this.distributingDamage = false;
    this.damageDistributionCardIndex = -1;
    this.damageDistributionCardName = '';
    this.damageDistributionXValue = 0;
    this.damageAssignments = new Map();
  }

  // ========== Multi-target selection ==========

  addMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    const current = this.multiTargetSelectedIds();
    if (current.includes(permanentId)) return; // Already selected
    if (current.length >= this.multiTargetMaxCount) return;
    this.multiTargetSelectedIds.set([...current, permanentId]);
  }

  removeMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    this.multiTargetSelectedIds.set(this.multiTargetSelectedIds().filter(id => id !== permanentId));
  }

  confirmMultiTargets(): void {
    if (!this.multiTargeting) return;
    const selected = this.multiTargetSelectedIds();
    if (selected.length < this.multiTargetMinCount) return;

    const card = this.pendingConvokeCard;
    this.pendingMultiTargetIds = [...selected];

    this.multiTargeting = false;
    this.multiTargetSelectedIds.set([]);

    // If card has convoke, enter convoke mode
    if (card?.hasConvoke) {
      this.enterConvokeMode(this.multiTargetCardIndex, card);
      return;
    }

    // Send directly
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.multiTargetCardIndex,
      targetPermanentIds: this.pendingMultiTargetIds
    });
    this.resetMultiTargetState();
  }

  cancelMultiTargeting(): void {
    this.multiTargeting = false;
    this.multiTargetCardIndex = -1;
    this.multiTargetCardName = '';
    this.multiTargetSelectedIds.set([]);
    this.pendingConvokeCard = null;
  }

  isMultiTargetSelected(permanentId: string): boolean {
    return this.multiTargetSelectedIds().includes(permanentId);
  }

  private resetMultiTargetState(): void {
    this.multiTargetCardIndex = -1;
    this.multiTargetCardName = '';
    this.multiTargetMinCount = 0;
    this.multiTargetMaxCount = 0;
    this.pendingMultiTargetIds = [];
    this.pendingConvokeCard = null;
  }

  // ========== Convoke selection ==========

  private enterConvokeMode(cardIndex: number, card: Card): void {
    this.convoking = true;
    this.convokeCardIndex = cardIndex;
    this.convokeCardName = card.name;
    this.convokeSelectedCreatureIds.set([]);
  }

  toggleConvokeCreature(permanentId: string): void {
    if (!this.convoking) return;
    const current = this.convokeSelectedCreatureIds();
    if (current.includes(permanentId)) {
      this.convokeSelectedCreatureIds.set(current.filter(id => id !== permanentId));
    } else {
      this.convokeSelectedCreatureIds.set([...current, permanentId]);
    }
  }

  isConvokeSelected(permanentId: string): boolean {
    return this.convokeSelectedCreatureIds().includes(permanentId);
  }

  confirmConvoke(): void {
    if (!this.convoking) return;
    const msg: any = {
      type: MessageType.PLAY_CARD,
      cardIndex: this.convokeCardIndex,
      convokeCreatureIds: this.convokeSelectedCreatureIds()
    };
    this.addPendingTargetsToMsg(msg);
    this.websocketService.send(msg);
    this.cancelConvoke();
    this.resetMultiTargetState();
  }

  skipConvoke(): void {
    if (!this.convoking) return;
    const msg: any = {
      type: MessageType.PLAY_CARD,
      cardIndex: this.convokeCardIndex
    };
    this.addPendingTargetsToMsg(msg);
    this.websocketService.send(msg);
    this.cancelConvoke();
    this.resetMultiTargetState();
  }

  private addPendingTargetsToMsg(msg: any): void {
    if (this.pendingMultiTargetIds.length > 0) {
      if (this.pendingConvokeCard && this.pendingConvokeCard.maxTargets > 1) {
        msg.targetPermanentIds = this.pendingMultiTargetIds;
      } else {
        // Single-target card that went through convoke flow
        msg.targetPermanentId = this.pendingMultiTargetIds[0];
      }
    }
  }

  cancelConvoke(): void {
    this.convoking = false;
    this.convokeCardIndex = -1;
    this.convokeCardName = '';
    this.convokeSelectedCreatureIds.set([]);
  }

  selectTarget(permanentId: string): void {
    if (!this.targeting) return;
    if (this.targetingForAbility) {
      const msg: any = {
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: permanentId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.websocketService.send(msg);
    } else if (this.pendingConvokeCard?.hasConvoke) {
      // Single-target spell with convoke — save target and enter convoke mode
      const cardIndex = this.targetingCardIndex;
      const card = this.pendingConvokeCard;
      this.pendingMultiTargetIds = [permanentId];
      this.targeting = false;
      this.targetingCardIndex = -1;
      this.targetingCardName = '';
      this.targetingForAbility = false;
      this.targetingRequiresAttacking = false;
      this.targetingAbilityIndex = -1;
      this.targetingAllowedTypes = [];
      this.targetingAllowedColors = [];
      this.pendingAbilityXValue = null;
      this.enterConvokeMode(cardIndex, card);
      return;
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingCardIndex,
        targetPermanentId: permanentId
      });
    }
    this.targeting = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingRequiresAttacking = false;
    this.targetingAbilityIndex = -1;
    this.targetingAllowedTypes = [];
    this.targetingAllowedColors = [];
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
  }

  selectPlayerTarget(playerIndex: number): void {
    if (!this.targeting || !this.targetingForPlayer) return;
    const g = this.gameSignal();
    if (!g) return;
    const playerId = g.playerIds[playerIndex];
    if (this.targetingForAbility) {
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: playerId
      });
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingCardIndex,
        targetPermanentId: playerId
      });
    }
    this.targeting = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingForPlayer = false;
    this.targetingRequiresAttacking = false;
    this.targetingAbilityIndex = -1;
    this.targetingAllowedTypes = [];
    this.targetingAllowedColors = [];
    this.pendingAbilityXValue = null;
  }

  cancelTargeting(): void {
    this.targeting = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingForPlayer = false;
    this.targetingRequiresAttacking = false;
    this.targetingAbilityIndex = -1;
    this.targetingAllowedTypes = [];
    this.targetingAllowedColors = [];
    this.pendingAbilityXValue = null;
  }

  selectSpellTarget(entry: StackEntry): void {
    if (!this.targetingSpell || !entry.isSpell) return;
    if (this.targetingForAbility) {
      this.websocketService.send({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingSpellCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetPermanentId: entry.cardId
      });
    } else {
      this.websocketService.send({
        type: MessageType.PLAY_CARD,
        cardIndex: this.targetingSpellCardIndex,
        targetPermanentId: entry.cardId
      });
    }
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  cancelSpellTargeting(): void {
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
  }

  isValidTarget(perm: Permanent): boolean {
    if (this.targetingRequiresAttacking) {
      return isPermanentCreature(perm) && perm.attacking;
    }
    if (this.targetingAllowedTypes.length > 0) {
      if (!this.targetingAllowedTypes.some(t => t.toUpperCase() === perm.card.type.toUpperCase())) {
        return false;
      }
      // For enchantment-only targeting (e.g., Aura Graft), only allow auras that are attached
      if (this.targetingAllowedTypes.length === 1 && perm.card.type === 'ENCHANTMENT' && perm.attachedTo == null) {
        return false;
      }
      return true;
    }
    if (this.targetingAllowedColors.length > 0) {
      return isPermanentCreature(perm) && perm.card.color != null
        && this.targetingAllowedColors.some(c => c.toUpperCase() === perm.card.color!.toUpperCase());
    }
    return isPermanentCreature(perm);
  }

  // ========== Tap / ability activation ==========

  tapPermanent(index: number): void {
    const g = this.gameSignal();
    if (g && this.canTapPermanent(index)) {
      const perm = this.myBattlefieldFn()[index];
      if (!perm) return;

      const abilities = perm.card.activatedAbilities;
      if (abilities.length === 0) {
        // No activated abilities — just tap for mana (ON_TAP)
        this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
        return;
      }

      // Filter to usable abilities
      const usable = abilities.filter(a => this.canUseAbility(perm, a));
      if (usable.length === 0) {
        // Has abilities but none usable — fall back to tap for mana if ON_TAP
        if (perm.card.hasTapAbility && !perm.tapped) {
          this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
        }
        return;
      }

      if (usable.length === 1) {
        // Single usable ability — activate directly
        const abilityIndex = abilities.indexOf(usable[0]);
        this.activateAbilityAtIndex(index, abilityIndex, perm);
      } else {
        // Multiple usable abilities — show picker
        this.choosingAbility = true;
        this.abilityChoicePermanentIndex = index;
        this.abilityChoices = abilities.map((a, i) => ({ ability: a, index: i, usable: this.canUseAbility(perm, a) }));
      }
    }
  }

  canUseAbility(perm: Permanent, ability: ActivatedAbilityView): boolean {
    if (ability.loyaltyCost != null) {
      const g = this.gameSignal();
      if (!g) return false;
      const myId = this.websocketService.currentUser?.userId;
      // Sorcery-speed: must be active player
      if (g.activePlayerId !== myId) return false;
      // Main phase only
      if (g.currentStep !== 'PRECOMBAT_MAIN' && g.currentStep !== 'POSTCOMBAT_MAIN') return false;
      // Stack must be empty
      if (g.stack.length > 0) return false;
      // Negative loyalty cost: check sufficient loyalty
      if (ability.loyaltyCost < 0 && perm.loyaltyCounters < Math.abs(ability.loyaltyCost)) return false;
      return true;
    }
    if (ability.requiresTap) {
      if (perm.tapped) return false;
      if (perm.summoningSick && isPermanentCreature(perm)) return false;
    }
    return true;
  }

  activateAbilityAtIndex(permanentIndex: number, abilityIndex: number, perm: Permanent): void {
    const ability = perm.card.activatedAbilities[abilityIndex];

    // Check for X cost
    const hasXCost = ability.manaCost?.includes('{X}') ?? false;
    if (hasXCost) {
      const baseCost = (ability.manaCost ?? '').replace('{X}', '');
      let base = 0;
      const matches = baseCost.match(/\{([^}]+)\}/g) || [];
      for (const m of matches) {
        const inner = m.slice(1, -1);
        const num = parseInt(inner);
        base += isNaN(num) ? 1 : num;
      }
      this.choosingXValue = true;
      this.xValueCardIndex = permanentIndex;
      this.xValueCardName = perm.card.name;
      this.xValueInput = 0;
      this.xValueMaximum = this.totalManaFn() - base;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Check for spell targeting (counter spells from abilities)
    if (ability.needsSpellTarget) {
      this.targetingSpell = true;
      this.targetingSpellCardIndex = permanentIndex;
      this.targetingSpellCardName = perm.card.name;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Check for targeting
    if (ability.needsTarget) {
      this.targeting = true;
      this.targetingCardIndex = permanentIndex;
      this.targetingCardName = perm.card.name;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      this.targetingForPlayer = ability.targetsPlayer;
      this.targetingAllowedTypes = ability.allowedTargetTypes ?? [];
      this.targetingAllowedColors = ability.allowedTargetColors ?? [];
      return;
    }

    // No target or X needed — send immediately
    this.websocketService.send({
      type: MessageType.ACTIVATE_ABILITY,
      permanentIndex,
      abilityIndex
    });
  }

  chooseAbility(choice: { ability: ActivatedAbilityView; index: number; usable: boolean }): void {
    if (!choice.usable) return;
    const perm = this.myBattlefieldFn()[this.abilityChoicePermanentIndex];
    if (!perm) return;
    this.activateAbilityAtIndex(this.abilityChoicePermanentIndex, choice.index, perm);
    this.choosingAbility = false;
    this.abilityChoicePermanentIndex = -1;
    this.abilityChoices = [];
  }

  cancelAbilityChoice(): void {
    this.choosingAbility = false;
    this.abilityChoicePermanentIndex = -1;
    this.abilityChoices = [];
  }

  canTapPermanent(index: number): boolean {
    const perm = this.myBattlefieldFn()[index];
    if (perm == null || !this.hasPriority) return false;
    const abilities = perm.card.activatedAbilities;
    // Has a non-tap activated ability (mana-cost only) — always usable
    if (abilities.some(a => !a.requiresTap)) return true;
    if (perm.tapped) return false;
    if (!perm.card.hasTapAbility) return false;
    if (perm.summoningSick && isPermanentCreature(perm)) return false;
    return true;
  }

  // ========== Library search ==========

  chooseLibraryCard(index: number): void {
    if (!this.searchingLibrary) return;
    this.websocketService.send({
      type: MessageType.LIBRARY_CARD_CHOSEN,
      cardIndex: index
    });
    this.searchingLibrary = false;
    this.librarySearchCards = [];
    this.librarySearchPrompt = '';
    this.librarySearchCanFailToFind = false;
  }

  declineLibrarySearch(): void {
    if (!this.searchingLibrary) return;
    this.websocketService.send({
      type: MessageType.LIBRARY_CARD_CHOSEN,
      cardIndex: -1
    });
    this.searchingLibrary = false;
    this.librarySearchCards = [];
    this.librarySearchPrompt = '';
    this.librarySearchCanFailToFind = false;
  }

  // ========== Library reorder ==========

  get reorderAvailableCards(): { card: Card; originalIndex: number }[] {
    return this.reorderAvailableIndices.map(i => ({ card: this.reorderAllCards[i], originalIndex: i }));
  }

  get reorderPlacedCards(): { card: Card; originalIndex: number; position: number }[] {
    return this.reorderOriginalIndices.map((origIdx, pos) => ({
      card: this.reorderAllCards[origIdx],
      originalIndex: origIdx,
      position: pos + 1
    }));
  }

  selectReorderCard(originalIndex: number): void {
    this.reorderOriginalIndices = [...this.reorderOriginalIndices, originalIndex];
    this.reorderAvailableIndices = this.reorderAvailableIndices.filter(i => i !== originalIndex);
  }

  undoLastReorderCard(): void {
    if (this.reorderOriginalIndices.length === 0) return;
    const lastIdx = this.reorderOriginalIndices[this.reorderOriginalIndices.length - 1];
    this.reorderOriginalIndices = this.reorderOriginalIndices.slice(0, -1);
    this.reorderAvailableIndices = [...this.reorderAvailableIndices, lastIdx];
  }

  confirmReorder(): void {
    this.websocketService.send({
      type: MessageType.LIBRARY_CARDS_REORDERED,
      cardOrder: this.reorderOriginalIndices
    });
    this.reorderingLibrary = false;
    this.reorderAllCards = [];
    this.reorderAvailableIndices = [];
    this.reorderOriginalIndices = [];
    this.reorderPrompt = '';
  }

  // ========== Hand/Top/Bottom (Telling Time) ==========

  get handTopBottomStep(): number {
    if (this.handTopBottomHandIndex === null) return 0;
    if (this.handTopBottomTopIndex === null) return 1;
    return 2;
  }

  get handTopBottomPrompt(): string {
    if (this.handTopBottomStep === 0) return 'Choose a card to put into your hand:';
    if (this.handTopBottomStep === 1) return 'Choose a card to put on top of your library:';
    return 'Confirm your choices:';
  }

  get handTopBottomAvailableCards(): { card: Card; originalIndex: number }[] {
    return this.handTopBottomCards
      .map((card, i) => ({ card, originalIndex: i }))
      .filter(item => item.originalIndex !== this.handTopBottomHandIndex && item.originalIndex !== this.handTopBottomTopIndex);
  }

  selectHandTopBottomCard(originalIndex: number): void {
    if (this.handTopBottomHandIndex === null) {
      this.handTopBottomHandIndex = originalIndex;
      // If only 2 cards total, auto-select the remaining one for top
      const remaining = this.handTopBottomCards
        .map((_, i) => i)
        .filter(i => i !== this.handTopBottomHandIndex);
      if (remaining.length === 1) {
        this.handTopBottomTopIndex = remaining[0];
      }
    } else if (this.handTopBottomTopIndex === null) {
      this.handTopBottomTopIndex = originalIndex;
    }
  }

  undoHandTopBottom(): void {
    if (this.handTopBottomTopIndex !== null) {
      this.handTopBottomTopIndex = null;
    } else if (this.handTopBottomHandIndex !== null) {
      this.handTopBottomHandIndex = null;
    }
  }

  confirmHandTopBottom(): void {
    if (this.handTopBottomHandIndex === null || this.handTopBottomTopIndex === null) return;
    this.websocketService.send({
      type: MessageType.HAND_TOP_BOTTOM_CHOSEN,
      handCardIndex: this.handTopBottomHandIndex,
      topCardIndex: this.handTopBottomTopIndex
    });
    this.choosingHandTopBottom = false;
    this.handTopBottomCards = [];
    this.handTopBottomHandIndex = null;
    this.handTopBottomTopIndex = null;
  }

  // ========== Reveal hand ==========

  closeRevealHand(): void {
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandPlayerName = '';
  }

  chooseFromRevealedHand(index: number): void {
    if (!this.choosingFromRevealedHand) return;
    if (!this.revealedHandChoosableIndices.has(index)) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      cardIndex: index
    });
    this.choosingFromRevealedHand = false;
    this.revealingHand = false;
    this.revealedHandCards = [];
    this.revealedHandChoosableIndices = new Set();
    this.revealedHandChoicePrompt = '';
  }

  isRevealedHandCardChoosable(index: number): boolean {
    return this.choosingFromRevealedHand && this.revealedHandChoosableIndices.has(index);
  }

  // ========== Graveyard choice ==========

  get graveyardChoiceCards(): { card: Card; index: number; owner: string }[] {
    const g = this.gameSignal();
    if (!g) return [];
    const allGraveyardCards: { card: Card; index: number; owner: string }[] = [];
    const validIndices = new Set(this.graveyardChoiceIndices);
    let poolIndex = 0;
    for (let playerIdx = 0; playerIdx < g.graveyards.length; playerIdx++) {
      const graveyard = g.graveyards[playerIdx];
      const ownerName = g.playerNames[playerIdx] ?? 'Unknown';
      for (const card of graveyard) {
        if (card.type === 'CREATURE' || card.type === 'ARTIFACT') {
          if (validIndices.has(poolIndex)) {
            allGraveyardCards.push({ card, index: poolIndex, owner: ownerName });
          }
          poolIndex++;
        }
      }
    }
    return allGraveyardCards;
  }

  chooseGraveyardCard(index: number): void {
    if (!this.choosingFromGraveyard) return;
    this.websocketService.send({
      type: MessageType.GRAVEYARD_CARD_CHOSEN,
      cardIndex: index
    });
    this.choosingFromGraveyard = false;
    this.graveyardChoiceIndices = [];
    this.graveyardChoicePrompt = '';
  }
}
