import { Injectable, Signal, signal } from '@angular/core';
import {
  WebsocketService, Game, MessageType, Card, Permanent, StackEntry, ActivatedAbilityView,
  ValidTargetsResponse, ModalOptionView
} from './websocket.service';
import { isPermanentArtifact, isPermanentCreature } from '../components/game/battlefield.utils';

@Injectable({ providedIn: 'root' })
export class TargetingChoiceService {

  constructor(private websocketService: WebsocketService) {}

  private gameSignal!: Signal<Game | null>;
  private myBattlefieldFn!: () => Permanent[];
  private opponentBattlefieldFn!: () => Permanent[];
  private totalManaFn!: () => number;
  private isStrictlyPlayableFn: (index: number) => boolean = () => false;
  private potentialTotalManaFn: () => number = () => 0;
  private potentialPayableAbilityIndicesFn: () => Record<string, number[]> = () => ({});

  init(
    gameSignal: Signal<Game | null>,
    myBattlefieldFn: () => Permanent[],
    opponentBattlefieldFn: () => Permanent[],
    totalManaFn: () => number,
    isStrictlyPlayableFn: (index: number) => boolean = () => false,
    potentialTotalManaFn: () => number = () => 0,
    potentialPayableAbilityIndicesFn: () => Record<string, number[]> = () => ({})
  ): void {
    this.gameSignal = gameSignal;
    this.myBattlefieldFn = myBattlefieldFn;
    this.opponentBattlefieldFn = opponentBattlefieldFn;
    this.totalManaFn = totalManaFn;
    this.isStrictlyPlayableFn = isStrictlyPlayableFn;
    this.potentialTotalManaFn = potentialTotalManaFn;
    this.potentialPayableAbilityIndicesFn = potentialPayableAbilityIndicesFn;
  }

  reset(): void {
    // Ability picker
    this.choosingAbility = false;
    this.abilityChoicePermanentIndex = -1;
    this.abilityChoices = [];
    // X cost
    this.choosingXValue = false;
    this.xValueCardIndex = -1;
    this.xValueCardName = '';
    this.xValueInput = 0;
    this.xValueMaximum = 0;
    this.graveyardXCardIndex = -1;
    // Targeting
    this.selectingTarget = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingAbilityXValue = null;
    this.validTargetIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = '';
    this.pendingTargetRequest = false;
    // Spell targeting
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
    // Multi-target
    this.multiTargeting = false;
    this.multiTargetCardIndex = -1;
    this.multiTargetCardName = '';
    this.multiTargetMinCount = 0;
    this.multiTargetMaxCount = 0;
    this.multiTargetSelectedIds.set([]);
    // Phyrexian mana
    this.choosingPhyrexianPayment = false;
    this.phyrexianCardIndex = -1;
    this.phyrexianCardName = '';
    this.phyrexianSymbolCount = 0;
    this.phyrexianLifePayCount = 0;
    this.pendingPhyrexianLifeCount = null;
    // Convoke
    this.convoking = false;
    this.convokeCardIndex = -1;
    this.convokeCardName = '';
    this.convokeSelectedCreatureIds.set([]);
    this.pendingMultiTargetIds = [];
    this.pendingConvokeCard = null;
    this.harmonizing = false;
    this.harmonizeCardIndex = -1;
    this.harmonizeCardName = '';
    this.harmonizeSelectedPermanentId.set(null);
    this.pendingHarmonizeCard = null;
    this.pendingHarmonizePermanentId = null;
    // Kicker
    this.choosingKicker = false;
    this.kickerCardIndex = -1;
    this.kickerCardName = '';
    this.kickerCost = '';
    this.pendingKicked = false;
    this.choosingKickerPermanent = false;
    this.kickerPermanentCardIndex = -1;
    this.kickerPermanentDescription = '';
    this.kickerPermanentSelectedId.set(null);
    this.pendingKickerPermanentId = null;
    // Buyback
    this.choosingBuyback = false;
    this.buybackCardIndex = -1;
    this.buybackCardName = '';
    this.buybackCost = '';
    this.buybackRequiresSacrifice = false;
    this.buybackSacrificeCount = 0;
    this.buybackDiscardCount = 0;
    this.choosingBuybackSacrifice = false;
    this.buybackSacrificeCardIndex = -1;
    this.buybackSacrificeDescription = '';
    this.buybackSacrificeSelectedIds.set([]);
    this.pendingBuybackSacrificePermanentId = null;
    this.pendingBuybackSacrificePermanentIds = [];
    this.choosingBuybackDiscard = false;
    this.buybackDiscardCardIndex = -1;
    this.buybackDiscardSelectedIndices.set([]);
    this.pendingBuybackDiscardHandIndices = null;
    this.pendingBuyback = false;
    // Modal mode picker
    this.choosingMode = false;
    this.modeCardIndex = -1;
    this.modeCardName = '';
    this.modeOptions = [];
    this.modeChoicesRequired = 1;
    this.modeChoicesMax = 1;
    this.modeOptional = false;
    this.modeSelectedIndices = [];
    this.spellTargetCount = 1;
    this.spellTargetSelectedIds = [];
    // Flashback
    this.pendingFlashback = false;
    this.selectingGraveyardCastDiscard = false;
    this.graveyardCastDiscardCardIndex = -1;
    this.graveyardCastDiscardCardName = '';
    this.pendingGraveyardCastDiscardHandIndex = null;
    // Exile / library-top casting
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
    this.pendingExileCounterCostPermanentIds = [];
    this.selectingExileCounterCost = false;
    this.exileCounterCostCardName = '';
    this.exileCounterCostRequired = 0;
    this.exileCounterCostSelectedIds.set([]);
    // Alternate casting cost
    this.choosingAlternateCost = false;
    this.selectingAlternateCostCreatures = false;
    this.selectingAlternateCostHandCard = false;
    this.alternateCostCardIndex = -1;
    this.alternateCostCardName = '';
    this.alternateCostSacrificeCount = 0;
    this.alternateCostLifePayment = 0;
    this.alternateCostTapCount = 0;
    this.alternateCostReturnCount = 0;
    this.alternateCostManaCost = '';
    this.alternateCostIsPlot = false;
    this.alternateCostExileHandCount = 0;
    this.alternateCostExileHandLabel = '';
    this.alternateCostRevealsHandCard = false;
    this.alternateCostDiscardsHandCard = false;
    this.alternateCostSelectedIds.set([]);
    this.choosingBehold = false;
    this.selectingBeholdPermanent = false;
    this.selectingBeholdHandCard = false;
    this.beholdCardIndex = -1;
    this.beholdCardName = '';
    this.beholdSubtype = '';
    this.beholdRequiredCount = 1;
    this.beholdSelectedCount = 0;
    this.beholdCardIsInGraveyard = false;
    this.pendingBeholdPermanentId = null;
    this.pendingBeholdHandCardIndex = null;
    this.pendingBeholdPermanentIds = [];
    this.pendingBeholdHandCardIndices = [];
    this.pendingBeholdChosenType = null;
    this.skipBeholdForCardIndex = null;
    this.beholdChosenCreatureType = false;
    this.beholdChosenType = '';
    this.pendingBeholdCard = null;
    this.pendingBeholdIsFlashback = false;
    this.choosingCreatureTypeOnly = false;
    // Graveyard targeting
    this.targetingGraveyard = false;
    this.graveyardTargetCards = [];
    this.graveyardTargetCardIds = [];
    this.graveyardTargetPrompt = '';
    // Exile targeting
    this.targetingExile = false;
    this.exileTargetCards = [];
    this.exileTargetCardIds = [];
    this.exileTargetPrompt = '';
    // MTGO-style cast payment
    this.clearCastPayment();
    this.clearAbilityPayment();
  }

  private get hasPriority(): boolean {
    const g = this.gameSignal();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
  }

  /**
   * True when priority has genuinely moved to someone else, as opposed to nobody holding it.
   *
   * The server reports `priorityPlayerId: null` for the whole time any interaction is awaiting
   * input — including a prompt raised by the player's own payment, such as the colour choice from
   * "{T}: Add one mana of any colour". Treating that as lost priority discarded the held-back cast
   * the moment a Birds of Paradise was tapped to pay for it, stranding the mana in the pool. Only
   * an explicitly different priority holder means the game moved on under us; a null holder means
   * "wait", so payment mode survives it and the pump simply doesn't fire (the cast still needs
   * strict playability, which the server withholds while the player is not acting).
   */
  private get priorityMovedOn(): boolean {
    const g = this.gameSignal();
    return g !== null && g.priorityPlayerId != null && !this.hasPriority;
  }

  /**
   * True while the player is partway through picking the target(s) for a spell or
   * ability they've started casting/activating. In the MTGO-style flow the mana is
   * tapped only *after* the target is locked in (payingForCast), so during target
   * selection the actionable, highlighted elements are the valid targets — not the
   * player's lands. Tapping mana here would produce nothing usable for the pending
   * cast and just muddies the board, so land tapping is suppressed until a target
   * is chosen (e.g. Burning Fields shows no green lands until a target is picked).
   */
  get selectingCastTarget(): boolean {
    return this.selectingTarget || this.targetingSpell || this.multiTargeting
      || this.targetingGraveyard || this.targetingExile || this.harmonizing;
  }

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
  // Graveyard activated ability with an X cost (e.g. Evershrike); -1 when the X prompt
  // is for a hand/battlefield source instead of a graveyard ability.
  graveyardXCardIndex = -1;

  // --- Modal mode picker state ---
  choosingMode = false;
  modeCardIndex = -1;
  modeCardName = '';
  modeOptions: ModalOptionView[] = [];
  modeChoicesRequired = 1;
  modeChoicesMax = 1;
  modeOptional = false;
  modeSelectedIndices: number[] = [];
  modeForAbility = false;
  modeAbilityPermanentIndex = -1;
  modeAbilityIndex = -1;
  // Multi-spell-target modal modes (e.g. "copy target instant and target creature spell")
  spellTargetCount = 1;
  spellTargetSelectedIds: string[] = [];

  // --- Targeting state (for instants and activated abilities) ---
  selectingTarget = false;
  targetingCardIndex = -1;
  targetingCardName = '';
  targetingForAbility = false;
  targetingForGraveyardAbility = false;
  targetingAbilityIndex = -1;
  pendingAbilityXValue: number | null = null;
  validTargetIds = signal(new Set<string>());
  validTargetPlayerIds = signal(new Set<string>());
  targetingPrompt = '';
  pendingTargetRequest = false;

  // --- Spell targeting state (for counterspells) ---
  targetingSpell = false;
  targetingSpellCardIndex = -1;
  targetingSpellCardName = '';

  // --- Multi-target state (for spells like "one or two target creatures") ---
  multiTargeting = false;
  multiTargetCardIndex = -1;
  multiTargetCardName = '';
  multiTargetMinCount = 0;
  multiTargetMaxCount = 0;
  multiTargetSelectedIds = signal<string[]>([]);

  // --- Phyrexian mana payment state ---
  choosingPhyrexianPayment = false;
  phyrexianCardIndex = -1;
  phyrexianCardName = '';
  phyrexianSymbolCount = 0;
  phyrexianLifePayCount = 0;
  private pendingPhyrexianLifeCount: number | null = null;

  // --- Convoke state ---
  convoking = false;
  convokeCardIndex = -1;
  convokeCardName = '';
  convokeSelectedCreatureIds = signal<string[]>([]);
  private pendingMultiTargetIds: string[] = [];
  private pendingConvokeCard: Card | null = null;

  harmonizing = false;
  harmonizeCardIndex = -1;
  harmonizeCardName = '';
  harmonizeSelectedPermanentId = signal<string | null>(null);
  private pendingHarmonizeCard: Card | null = null;
  private pendingHarmonizePermanentId: string | null = null;

  // --- Kicker state ---
  choosingKicker = false;
  kickerCardIndex = -1;
  kickerCardName = '';
  kickerCost = '';
  private pendingKicked = false;
  choosingKickerPermanent = false;
  kickerPermanentCardIndex = -1;
  kickerPermanentDescription = '';
  kickerPermanentSelectedId = signal<string | null>(null);
  private pendingKickerPermanentId: string | null = null;

  choosingBuyback = false;
  buybackCardIndex = -1;
  buybackCardName = '';
  buybackCost = '';
  buybackRequiresSacrifice = false;
  buybackSacrificeCount = 0;
  buybackDiscardCount = 0;
  choosingBuybackSacrifice = false;
  buybackSacrificeCardIndex = -1;
  buybackSacrificeDescription = '';
  buybackSacrificeSelectedIds = signal<string[]>([]);
  private pendingBuybackSacrificePermanentId: string | null = null;
  private pendingBuybackSacrificePermanentIds: string[] = [];
  choosingBuybackDiscard = false;
  buybackDiscardCardIndex = -1;
  buybackDiscardSelectedIndices = signal<number[]>([]);
  private pendingBuybackDiscardHandIndices: number[] | null = null;
  private pendingBuyback = false;

  // --- Flashback state ---
  private pendingFlashback = false;
  selectingGraveyardCastDiscard = false;
  graveyardCastDiscardCardIndex = -1;
  graveyardCastDiscardCardName = '';
  private pendingGraveyardCastDiscardHandIndex: number | null = null;

  // --- Exile / library-top casting state ---
  private pendingFromExileCardId: string | null = null;
  private pendingFromLibraryTop = false;
  private pendingZoneCard: Card | null = null;
  selectingExileCounterCost = false;
  exileCounterCostCardName = '';
  exileCounterCostRequired = 0;
  exileCounterCostSelectedIds = signal<string[]>([]);
  private pendingExileCounterCostPermanentIds: string[] = [];

  // --- Alternate casting cost state ---
  choosingAlternateCost = false;
  selectingAlternateCostCreatures = false;
  alternateCostCardIndex = -1;
  alternateCostCardName = '';
  alternateCostSacrificeCount = 0;
  alternateCostLifePayment = 0;
  alternateCostTapCount = 0;
  alternateCostReturnCount = 0;
  alternateCostManaCost = '';
  alternateCostIsPlot = false;
  alternateCostExileHandCount = 0;
  alternateCostExileHandLabel = '';
  alternateCostRevealsHandCard = false;
  alternateCostDiscardsHandCard = false;
  alternateCostRequiresTarget = false;
  alternateCostSelectedIds = signal<string[]>([]);
  alternateCostSelectedHandIndices = signal<number[]>([]);
  selectingAlternateCostHandCard = false;
  /** Hand index to exile when confirming an exile-from-hand alternate cast (pre-removal index). */
  private pendingAlternateExileHandIndex: number | null = null;
  private pendingAlternateHandCardIndices: number[] = [];
  private pendingAlternateHandCardDiscards = false;
  private pendingAlternateExileHandIndices: number[] = [];

  choosingBehold = false;
  selectingBeholdPermanent = false;
  selectingBeholdHandCard = false;
  beholdCardIndex = -1;
  beholdCardName = '';
  beholdSubtype = '';
  beholdChosenCreatureType = false;
  beholdChosenType = '';
  beholdRequiredCount = 1;
  beholdSelectedCount = 0;
  beholdCardIsInGraveyard = false;
  private pendingBeholdPermanentId: string | null = null;
  private pendingBeholdHandCardIndex: number | null = null;
  private pendingBeholdPermanentIds: string[] = [];
  private pendingBeholdHandCardIndices: number[] = [];
  private pendingBeholdChosenType: string | null = null;
  private skipBeholdForCardIndex: number | null = null;
  private pendingBeholdCard: Card | null = null;
  private pendingBeholdIsFlashback = false;
  choosingCreatureTypeOnly = false;

  // --- Graveyard targeting state ---
  targetingGraveyard = false;
  graveyardTargetCards: Card[] = [];
  graveyardTargetCardIds: string[] = [];
  graveyardTargetPrompt = '';

  // --- Exile targeting state ---
  targetingExile = false;
  exileTargetCards: Card[] = [];
  exileTargetCardIds: string[] = [];
  exileTargetPrompt = '';

  // --- MTGO-style cast payment state ---
  // A fully specified PLAY_CARD message waiting for the mana cost to be covered: the
  // player clicked a card only "potentially" playable (affordable if they tap their mana
  // sources), finished all pre-cast choices (modes/kicker/X/targets), and is now tapping
  // lands. The message is sent automatically once the pool covers the cost; the side
  // panel shows a Cancel button (instead of Pass Priority) that reverts the taps.
  payingForCast = false;
  pendingCastCardIndex = -1;
  pendingCastCardName = '';
  private pendingCastCardId: string | null = null;
  /** Cost to re-check client-side before firing (X/kicker portion); null = trust strict playability. */
  private pendingCastManaCost: string | null = null;
  private pendingCastXValue = 0;
  private pendingCastMessage: any = null;

  // --- MTGO-style ability activation payment state ---
  // The ACTIVATE_ABILITY counterpart of the cast payment above: the activation is fully
  // specified (X announced, targets locked in) but the mana cost isn't covered by the
  // pool yet, so the message is held back while the player taps mana sources. It fires
  // automatically once the pool covers the cost; Cancel reverts the taps.
  payingForAbility = false;
  pendingActivationSourceName = '';
  pendingActivationPermanentId: string | null = null;
  private pendingActivationManaCost: string | null = null;
  private pendingActivationXValue = 0;
  private pendingActivationRequiresTap = false;
  private pendingActivationMessage: any = null;

  // ========== Message handlers ==========

  handleValidTargetsResponse(msg: ValidTargetsResponse): void {
    this.pendingTargetRequest = false;

    const hasGraveyardTargets = msg.validGraveyardCardIds && msg.validGraveyardCardIds.length > 0;
    const hasExileTargets = msg.validExiledCardIds && msg.validExiledCardIds.length > 0;

    // No valid targets — auto-cancel to prevent stuck UI
    if (msg.validPermanentIds.length === 0 && msg.validPlayerIds.length === 0
        && !hasGraveyardTargets && !hasExileTargets && msg.minTargets > 0) {
      this.resetTargetingState();
      this.cancelMultiTargeting();
      return;
    }

    // Exile targeting: source-tracked exiled cards are displayed under their permanent.
    if (hasExileTargets) {
      const g = this.gameSignal();
      if (g) {
        const validIds = new Set(msg.validExiledCardIds);
        const cards: Card[] = [];
        const cardIds: string[] = [];
        for (const battlefield of g.battlefields) {
          for (const permanent of battlefield) {
            for (const card of permanent.exiledWithCards ?? []) {
              if (card.id && validIds.has(card.id)) {
                cards.push(card);
                cardIds.push(card.id);
              }
            }
          }
        }
        this.targetingExile = true;
        this.exileTargetCards = cards;
        this.exileTargetCardIds = cardIds;
        this.exileTargetPrompt = msg.prompt;
      }
      return;
    }

    // Graveyard targeting: show graveyard cards as targets in an overlay
    if (hasGraveyardTargets) {
      const g = this.gameSignal();
      if (g) {
        const validIds = new Set(msg.validGraveyardCardIds);
        const cards: Card[] = [];
        const cardIds: string[] = [];
        for (const graveyard of g.graveyards) {
          for (const card of graveyard) {
            if (card.id && validIds.has(card.id)) {
              cards.push(card);
              cardIds.push(card.id);
            }
          }
        }
        this.targetingGraveyard = true;
        this.graveyardTargetCards = cards;
        this.graveyardTargetCardIds = cardIds;
        this.graveyardTargetPrompt = msg.prompt;
      }
      return;
    }

    this.validTargetIds.set(new Set(msg.validPermanentIds));
    this.validTargetPlayerIds.set(new Set(msg.validPlayerIds));
    this.targetingPrompt = msg.prompt;

    if (msg.maxTargets > 1) {
      // Multi-target mode. Responses also arrive as refreshes after each pick
      // (addMultiTarget/removeMultiTarget re-request valid targets) — only clear
      // the selection when first entering the mode, or Confirm becomes unreachable.
      if (!this.multiTargeting) {
        this.multiTargeting = true;
        this.multiTargetCardIndex = this.targetingCardIndex;
        this.multiTargetCardName = this.targetingCardName;
        this.multiTargetSelectedIds.set([]);
      }
      this.multiTargetMinCount = msg.minTargets;
      this.multiTargetMaxCount = msg.maxTargets;
    } else {
      // Single target mode
      this.selectingTarget = true;
    }
  }

  // ========== Play card / targeting / abilities ==========

  private sendValidTargetsRequest(cardIndex: number | null, permanentIndex: number | null, abilityIndex: number | null, alreadySelectedIds: string[] = [], xValue: number | null = null, graveyardCardIndex: number | null = null): void {
    this.pendingTargetRequest = true;
    const msg: any = {
      type: MessageType.VALID_TARGETS_REQUEST,
      cardIndex,
      permanentIndex,
      abilityIndex,
      alreadySelectedIds
    };
    if (graveyardCardIndex != null) {
      msg.graveyardCardIndex = graveyardCardIndex;
    }
    if (xValue != null) {
      msg.xValue = xValue;
    }
    if (this.pendingKicked) {
      msg.kicked = true;
    } else if (cardIndex != null && permanentIndex == null) {
      // Explicitly send kicked=false for spells from hand (not abilities)
      // so the backend can resolve KickerReplacementEffect to the base effect
      msg.kicked = false;
    }
    this.websocketService.send(msg);
  }

  playCard(index: number, isCardPlayable: (i: number) => boolean): void {
    // While paying for a held-back cast/activation, hand clicks are ignored — cancel first.
    if (this.payingForCast || this.payingForAbility) return;
    const g = this.gameSignal();
    if (g && isCardPlayable(index)) {
      const card = g.hand[index];

      // Check for alternate casting cost — offer choice before anything else
      if (card.hasAlternateCastingCost) {
        this.choosingAlternateCost = true;
        this.alternateCostCardIndex = index;
        this.alternateCostCardName = card.name;
        this.alternateCostSacrificeCount = card.alternateCostSacrificeCount;
        this.alternateCostLifePayment = card.alternateCostLifePayment;
        this.alternateCostTapCount = card.alternateCostTapCount;
        this.alternateCostReturnCount = card.alternateCostReturnCount;
        this.alternateCostManaCost = card.alternateCostManaCost ?? '';
        this.alternateCostIsPlot = card.keywords.includes('PLOT');
        this.alternateCostExileHandCount = card.alternateCostExileHandCount ?? 0;
        this.alternateCostExileHandLabel = card.alternateCostExileHandLabel ?? '';
        this.alternateCostRevealsHandCard = card.alternateCostRevealsHandCard ?? false;
        this.alternateCostDiscardsHandCard = card.alternateCostDiscardsHandCard ?? false;
        this.alternateCostRequiresTarget = card.alternateCostRequiresTarget ?? false;
        return;
      }

      // Check for Phyrexian mana — show chooser before anything else
      if (card.hasPhyrexianMana && card.phyrexianManaCount > 0) {
        this.choosingPhyrexianPayment = true;
        this.phyrexianCardIndex = index;
        this.phyrexianCardName = card.name;
        this.phyrexianSymbolCount = card.phyrexianManaCount;
        this.phyrexianLifePayCount = 0;
        return;
      }

      // Check for kicker — offer choice before continuing
      if (card.kickerCost) {
        this.choosingKicker = true;
        this.kickerCardIndex = index;
        this.kickerCardName = card.name;
        this.kickerCost = card.kickerCost;
        return;
      }

      // Check for buyback — offer choice before continuing
      if (card.buybackCost) {
        this.choosingBuyback = true;
        this.buybackCardIndex = index;
        this.buybackCardName = card.name;
        this.buybackCost = card.buybackCost;
        this.buybackRequiresSacrifice = card.buybackRequiresSacrifice ?? false;
        this.buybackSacrificeCount = card.buybackSacrificeCount ?? 1;
        this.buybackDiscardCount = card.buybackDiscardCount ?? 0;
        return;
      }

      this.continuePlayCard(index);
    }
  }

  private continuePlayCard(index: number): void {
    const g = this.gameSignal();
    if (!g) return;
    const card = g.hand[index];
    if (!card) return;

    if ((card.additionalBeholdSubtype || card.additionalBeholdChosenCreatureType || card.additionalChooseCreatureType) && !card.additionalBeholdFlashbackOnly
        && this.skipBeholdForCardIndex !== index
        && this.pendingBeholdPermanentId == null && this.pendingBeholdHandCardIndex == null
        && this.pendingBeholdPermanentIds.length === 0 && this.pendingBeholdHandCardIndices.length === 0) {
      this.beginBeholdSelection(card, index, false);
      return;
    }
    if (this.skipBeholdForCardIndex === index) this.skipBeholdForCardIndex = null;

    // Modal ("choose one/two") spell or ETB — pick mode(s) before anything else
    if (card.modalChoicesRequired > 0 && card.modalOptions && card.modalOptions.length > 0) {
      this.choosingMode = true;
      this.modeCardIndex = index;
      this.modeCardName = card.name;
      this.modeOptions = card.modalOptions;
      this.modeChoicesRequired = card.modalChoicesRequired;
      this.modeChoicesMax = card.modalChoicesMax > 0 ? card.modalChoicesMax : card.modalChoicesRequired;
      this.modeOptional = card.modalOptional;
      this.modeSelectedIndices = [];
      return;
    }

    if (card.requiresXValue === true) {
      this.choosingXValue = true;
      this.xValueCardIndex = index;
      this.xValueCardName = card.name;
      this.xValueInput = card.xValueMin ?? 0;
      this.xValueMaximum = card.xValueMax ?? 0;
      return;
    }

    const hasXCost = card.manaCost?.includes('{X}') ?? false;

    if (hasXCost) {
      const baseCost = (card.manaCost ?? '').replace('{X}', '');
      let base = 0;
      const matches = baseCost.match(/\{([^}]+)\}/g) || [];
      for (const m of matches) {
        const inner = m.slice(1, -1);
        const num = parseInt(inner);
        base += isNaN(num) ? 1 : num;
      }
      this.choosingXValue = true;
      this.xValueCardIndex = index;
      this.xValueCardName = card.name;
      this.xValueInput = 0;
      // X can be paid MTGO-style by tapping more lands after announcing, so the cap is
      // the potential mana (pool + untapped sources), not just what's floating now.
      this.xValueMaximum = Math.max(this.totalManaFn(), this.potentialTotalManaFn()) - base;
      return;
    }
    if (card.needsSpellTarget) {
      this.targetingSpell = true;
      this.targetingSpellCardIndex = index;
      this.targetingSpellCardName = card.name;
      return;
    }
    if (card.needsTarget) {
      // Ask backend for valid targets
      this.targetingCardIndex = index;
      this.targetingCardName = card.name;
      this.targetingForAbility = false;
      this.targetingAbilityIndex = -1;
      this.pendingAbilityXValue = null;
      this.pendingConvokeCard = card.hasConvoke ? card : null;
      this.sendValidTargetsRequest(index, null, null);
      return;
    }
    // No targets needed — check for convoke
    if (card.hasConvoke) {
      this.pendingConvokeCard = card;
      this.pendingMultiTargetIds = [];
      this.enterConvokeMode(index, card);
      return;
    }
    this.sendPlayCardMessage(index, null);
  }

  confirmPhyrexianPayment(): void {
    this.pendingPhyrexianLifeCount = this.phyrexianLifePayCount > 0 ? this.phyrexianLifePayCount : 0;
    const savedIndex = this.phyrexianCardIndex;
    this.choosingPhyrexianPayment = false;
    this.phyrexianCardIndex = -1;
    this.phyrexianCardName = '';
    this.phyrexianSymbolCount = 0;
    this.phyrexianLifePayCount = 0;
    this.continuePlayCard(savedIndex);
  }

  cancelPhyrexianPayment(): void {
    this.choosingPhyrexianPayment = false;
    this.phyrexianCardIndex = -1;
    this.phyrexianCardName = '';
    this.phyrexianSymbolCount = 0;
    this.phyrexianLifePayCount = 0;
    this.pendingPhyrexianLifeCount = null;
  }

  confirmKicker(): void {
    this.pendingKicked = true;
    const savedIndex = this.kickerCardIndex;
    const card = this.gameSignal()?.hand[savedIndex];
    this.choosingKicker = false;
    this.kickerCardIndex = -1;
    this.kickerCardName = '';
    this.kickerCost = '';
    if (card?.kickerRequiresTap || card?.kickerRequiresReturn) {
      this.choosingKickerPermanent = true;
      this.kickerPermanentCardIndex = savedIndex;
      this.kickerPermanentDescription = card.kickerCost ?? '';
      this.kickerPermanentSelectedId.set(null);
      return;
    }
    this.continuePlayCard(savedIndex);
  }

  skipKicker(): void {
    this.pendingKicked = false;
    const savedIndex = this.kickerCardIndex;
    this.choosingKicker = false;
    this.kickerCardIndex = -1;
    this.kickerCardName = '';
    this.kickerCost = '';
    this.continuePlayCard(savedIndex);
  }

  cancelKicker(): void {
    this.choosingKicker = false;
    this.kickerCardIndex = -1;
    this.kickerCardName = '';
    this.kickerCost = '';
    this.pendingKicked = false;
    this.pendingKickerPermanentId = null;
  }

  toggleKickerPermanent(permanentId: string): void {
    if (!this.choosingKickerPermanent) return;
    this.kickerPermanentSelectedId.set(
      this.kickerPermanentSelectedId() === permanentId ? null : permanentId);
  }

  canSelectKickerPermanent(permanent: Permanent): boolean {
    if (!this.choosingKickerPermanent) return false;
    const card = this.gameSignal()?.hand[this.kickerPermanentCardIndex];
    return card?.kickerRequiresReturn ? isPermanentCreature(permanent) : !permanent.tapped;
  }

  confirmKickerPermanent(): void {
    const selectedId = this.kickerPermanentSelectedId();
    if (!this.choosingKickerPermanent || selectedId == null) return;
    const savedIndex = this.kickerPermanentCardIndex;
    this.pendingKickerPermanentId = selectedId;
    this.choosingKickerPermanent = false;
    this.kickerPermanentCardIndex = -1;
    this.kickerPermanentDescription = '';
    this.kickerPermanentSelectedId.set(null);
    this.continuePlayCard(savedIndex);
  }

  cancelKickerPermanent(): void {
    this.choosingKickerPermanent = false;
    this.kickerPermanentCardIndex = -1;
    this.kickerPermanentDescription = '';
    this.kickerPermanentSelectedId.set(null);
    this.pendingKicked = false;
    this.pendingKickerPermanentId = null;
  }

  confirmBuyback(): void {
    this.pendingBuyback = true;
    const savedIndex = this.buybackCardIndex;
    const requiresSacrifice = this.buybackRequiresSacrifice;
    const sacrificeCount = this.buybackSacrificeCount;
    const discardCount = this.buybackDiscardCount;
    const sacrificeDescription = this.buybackCost.replace(/^Sacrifice /, '');
    this.choosingBuyback = false;
    this.buybackCardIndex = -1;
    this.buybackCardName = '';
    this.buybackCost = '';
    if (requiresSacrifice) {
      this.choosingBuybackSacrifice = true;
      this.buybackSacrificeCardIndex = savedIndex;
      this.buybackSacrificeDescription = sacrificeDescription;
      this.buybackSacrificeCount = sacrificeCount;
      this.buybackSacrificeSelectedIds.set([]);
      return;
    }
    if (discardCount > 0) {
      this.choosingBuybackDiscard = true;
      this.buybackDiscardCardIndex = savedIndex;
      this.buybackDiscardSelectedIndices.set([]);
      return;
    }
    this.continuePlayCard(savedIndex);
  }

  skipBuyback(): void {
    this.pendingBuyback = false;
    const savedIndex = this.buybackCardIndex;
    this.choosingBuyback = false;
    this.buybackCardIndex = -1;
    this.buybackCardName = '';
    this.buybackCost = '';
    this.buybackRequiresSacrifice = false;
    this.buybackSacrificeCount = 0;
    this.buybackDiscardCount = 0;
    this.buybackDiscardSelectedIndices.set([]);
    this.pendingBuybackDiscardHandIndices = null;
    this.continuePlayCard(savedIndex);
  }

  cancelBuyback(): void {
    this.choosingBuyback = false;
    this.buybackCardIndex = -1;
    this.buybackCardName = '';
    this.buybackCost = '';
    this.buybackRequiresSacrifice = false;
    this.buybackSacrificeCount = 0;
    this.buybackDiscardCount = 0;
    this.buybackDiscardSelectedIndices.set([]);
    this.pendingBuybackDiscardHandIndices = null;
    this.pendingBuyback = false;
  }

  toggleBuybackSacrifice(permanentId: string): void {
    if (!this.choosingBuybackSacrifice) return;
    const selected = this.buybackSacrificeSelectedIds();
    if (selected.includes(permanentId)) {
      this.buybackSacrificeSelectedIds.set(selected.filter(id => id !== permanentId));
    } else if (selected.length < this.buybackSacrificeCount) {
      this.buybackSacrificeSelectedIds.set([...selected, permanentId]);
    }
  }

  isBuybackSacrificeSelected(permanentId: string): boolean {
    return this.choosingBuybackSacrifice && this.buybackSacrificeSelectedIds().includes(permanentId);
  }

  confirmBuybackSacrifice(): void {
    const selectedIds = this.buybackSacrificeSelectedIds();
    if (!this.choosingBuybackSacrifice || selectedIds.length !== this.buybackSacrificeCount) return;
    const savedIndex = this.buybackSacrificeCardIndex;
    if (this.buybackSacrificeCount === 1) {
      this.pendingBuybackSacrificePermanentId = selectedIds[0];
      this.pendingBuybackSacrificePermanentIds = [];
    } else {
      this.pendingBuybackSacrificePermanentId = null;
      this.pendingBuybackSacrificePermanentIds = [...selectedIds];
    }
    this.choosingBuybackSacrifice = false;
    this.buybackSacrificeCardIndex = -1;
    this.buybackSacrificeDescription = '';
    this.buybackSacrificeSelectedIds.set([]);
    this.buybackRequiresSacrifice = false;
    this.buybackSacrificeCount = 0;
    this.continuePlayCard(savedIndex);
  }

  toggleBuybackDiscard(handIndex: number): void {
    if (!this.choosingBuybackDiscard || handIndex === this.buybackDiscardCardIndex) return;
    const selected = this.buybackDiscardSelectedIndices();
    if (selected.includes(handIndex)) {
      this.buybackDiscardSelectedIndices.set(selected.filter(index => index !== handIndex));
    } else if (selected.length < this.buybackDiscardCount) {
      this.buybackDiscardSelectedIndices.set([...selected, handIndex]);
    }
  }

  isBuybackDiscardSelected(handIndex: number): boolean {
    return this.choosingBuybackDiscard && this.buybackDiscardSelectedIndices().includes(handIndex);
  }

  isBuybackDiscardSelectable(handIndex: number): boolean {
    return this.choosingBuybackDiscard && handIndex !== this.buybackDiscardCardIndex;
  }

  confirmBuybackDiscard(): void {
    const selected = this.buybackDiscardSelectedIndices();
    if (!this.choosingBuybackDiscard || selected.length !== this.buybackDiscardCount) return;
    const savedIndex = this.buybackDiscardCardIndex;
    this.pendingBuybackDiscardHandIndices = [...selected];
    this.choosingBuybackDiscard = false;
    this.buybackDiscardCardIndex = -1;
    this.buybackDiscardSelectedIndices.set([]);
    this.buybackDiscardCount = 0;
    this.continuePlayCard(savedIndex);
  }

  cancelBuybackDiscard(): void {
    this.choosingBuybackDiscard = false;
    this.buybackDiscardCardIndex = -1;
    this.buybackDiscardSelectedIndices.set([]);
    this.buybackDiscardCount = 0;
    this.pendingBuybackDiscardHandIndices = null;
    this.pendingBuyback = false;
  }

  cancelBuybackSacrifice(): void {
    this.choosingBuybackSacrifice = false;
    this.buybackSacrificeCardIndex = -1;
    this.buybackSacrificeDescription = '';
    this.buybackSacrificeSelectedIds.set([]);
    this.buybackSacrificeCount = 0;
    this.pendingBuyback = false;
    this.pendingBuybackSacrificePermanentId = null;
    this.pendingBuybackSacrificePermanentIds = [];
    this.choosingBuybackDiscard = false;
    this.buybackDiscardCardIndex = -1;
    this.buybackDiscardSelectedIndices.set([]);
    this.pendingBuybackDiscardHandIndices = null;
  }

  // ========== Modal mode picker ==========

  toggleMode(optionIndex: number): void {
    if (!this.choosingMode) return;
    if (this.modeChoicesRequired === 1 && this.modeChoicesMax === 1) {
      this.modeSelectedIndices = [optionIndex];
      return;
    }
    if (this.modeSelectedIndices.includes(optionIndex)) {
      this.modeSelectedIndices = this.modeSelectedIndices.filter(i => i !== optionIndex);
    } else if (this.modeSelectedIndices.length < this.modeChoicesMax) {
      this.modeSelectedIndices = [...this.modeSelectedIndices, optionIndex];
    }
  }

  isModeSelected(optionIndex: number): boolean {
    return this.modeSelectedIndices.includes(optionIndex);
  }

  /**
   * Encodes the mode selection the same way the engine's ChooseOneEffect.encodeModeSelection
   * does: exact choose-one uses the 0-based mode index; choose-two / one-or-more use a
   * negative bitmask (including selecting a single mode of a one-or-more spell).
   */
  private encodeModeSelection(indices: number[]): number {
    if (this.modeChoicesRequired === 1 && this.modeChoicesMax === 1) {
      return indices[0];
    }
    let mask = 0;
    for (const i of indices) {
      mask |= (1 << i);
    }
    return -mask;
  }

  confirmModes(): void {
    if (!this.choosingMode
        || this.modeSelectedIndices.length < this.modeChoicesRequired
        || this.modeSelectedIndices.length > this.modeChoicesMax) return;
    const g = this.gameSignal();
    if (!g) return;

    const cardIndex = this.modeCardIndex;
    const cardName = this.modeCardName;
    const zoneCard = this.pendingZoneCard;
    const card = zoneCard ?? g.hand[cardIndex];
    const modeForAbility = this.modeForAbility;
    const abilityPermanentIndex = this.modeAbilityPermanentIndex;
    const abilityIndex = this.modeAbilityIndex;
    const chosen = this.modeSelectedIndices.map(i => this.modeOptions[i]);
    const encoded = this.encodeModeSelection(this.modeSelectedIndices);
    this.resetModeState();

    if (modeForAbility) {
      if (chosen.some(o => o.needsTarget)) {
        this.targetingCardIndex = abilityPermanentIndex;
        this.targetingCardName = cardName;
        this.targetingForAbility = true;
        this.targetingAbilityIndex = abilityIndex;
        this.pendingAbilityXValue = encoded;
        this.multiTargetCardIndex = abilityPermanentIndex;
        this.multiTargetCardName = cardName;
        this.sendValidTargetsRequest(null, abilityPermanentIndex, abilityIndex, [], encoded);
      } else {
        this.sendActivateAbilityMessage({
          type: MessageType.ACTIVATE_ABILITY,
          permanentIndex: abilityPermanentIndex,
          abilityIndex,
          xValue: encoded
        });
      }
      return;
    }

    if (chosen.some(o => o.needsSpellTarget)) {
      // Works for zone plays too: selectSpellTarget sends via sendPlayCardMessage,
      // which attaches the pending fromExileCardId/fromLibraryTop flags.
      this.targetingSpell = true;
      this.targetingSpellCardIndex = cardIndex;
      this.targetingSpellCardName = cardName;
      this.pendingAbilityXValue = encoded;
      this.spellTargetCount = Math.max(...chosen.map(o => o.targetCount));
      this.spellTargetSelectedIds = [];
      return;
    }
    if (chosen.some(o => o.needsTarget)) {
      if (zoneCard) {
        // Zone plays can't use VALID_TARGETS_REQUEST (it only knows hand cards)
        this.pendingZoneCard = null;
        this.enterZoneTargeting(zoneCard, encoded);
        return;
      }
      this.targetingCardIndex = cardIndex;
      this.targetingCardName = cardName;
      this.targetingForAbility = false;
      this.targetingAbilityIndex = -1;
      this.pendingAbilityXValue = encoded;
      this.pendingConvokeCard = card?.hasConvoke ? card : null;
      this.sendValidTargetsRequest(cardIndex, null, null, [], encoded);
      return;
    }
    if (!zoneCard && card?.hasConvoke) {
      this.pendingAbilityXValue = encoded;
      this.pendingConvokeCard = card;
      this.pendingMultiTargetIds = [];
      this.enterConvokeMode(cardIndex, card);
      return;
    }
    this.sendPlayCardMessage(cardIndex, null, { xValue: encoded });
  }

  /** "Choose up to one" — decline to pick any mode (engine encodes the skip as -1). */
  skipModes(): void {
    if (!this.choosingMode || !this.modeOptional) return;
    const cardIndex = this.modeCardIndex;
    this.resetModeState();
    this.sendPlayCardMessage(cardIndex, null, { xValue: -1 });
  }

  cancelModes(): void {
    this.resetModeState();
    this.pendingPhyrexianLifeCount = null;
    this.pendingKicked = false;
    this.pendingBuyback = false;
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
    this.pendingExileCounterCostPermanentIds = [];
  }

  private resetModeState(): void {
    this.choosingMode = false;
    this.modeCardIndex = -1;
    this.modeCardName = '';
    this.modeOptions = [];
    this.modeChoicesRequired = 1;
    this.modeChoicesMax = 1;
    this.modeOptional = false;
    this.modeSelectedIndices = [];
    this.modeForAbility = false;
    this.modeAbilityPermanentIndex = -1;
    this.modeAbilityIndex = -1;
  }

  startFlashbackTargeting(graveyardIndex: number, card: Card): void {
    this.pendingFlashback = true;
    if (card.graveyardCastRequiresDiscard) {
      this.selectingGraveyardCastDiscard = true;
      this.graveyardCastDiscardCardIndex = graveyardIndex;
      this.graveyardCastDiscardCardName = card.name;
      return;
    }
    if (card.hasHarmonize) {
      this.startHarmonizeSelection(graveyardIndex, card);
      return;
    }
    this.continueFlashbackPlay(graveyardIndex, card);
  }

  selectGraveyardCastDiscardHandCard(handIndex: number): void {
    if (!this.selectingGraveyardCastDiscard) return;
    const graveyardIndex = this.graveyardCastDiscardCardIndex;
    const game = this.gameSignal();
    const playerIndex = game?.playerIds.indexOf(this.websocketService.currentUser?.userId ?? '') ?? -1;
    const card = playerIndex >= 0 ? game?.graveyards[playerIndex]?.[graveyardIndex] : undefined;
    if (!card) return;
    this.pendingGraveyardCastDiscardHandIndex = handIndex;
    this.selectingGraveyardCastDiscard = false;
    this.graveyardCastDiscardCardIndex = -1;
    this.graveyardCastDiscardCardName = '';
    if (card.hasHarmonize) {
      this.startHarmonizeSelection(graveyardIndex, card);
    } else if (card.needsTarget || card.additionalBeholdFlashbackOnly) {
      this.continueFlashbackPlay(graveyardIndex, card);
    } else {
      this.sendPlayCardMessage(graveyardIndex, null);
    }
  }

  cancelGraveyardCastDiscard(): void {
    this.selectingGraveyardCastDiscard = false;
    this.graveyardCastDiscardCardIndex = -1;
    this.graveyardCastDiscardCardName = '';
    this.pendingGraveyardCastDiscardHandIndex = null;
    this.pendingFlashback = false;
  }

  private continueFlashbackPlay(graveyardIndex: number, card: Card): void {
    if (card.additionalBeholdSubtype && card.additionalBeholdFlashbackOnly
        && this.pendingBeholdPermanentId == null && this.pendingBeholdHandCardIndex == null
        && this.pendingBeholdPermanentIds.length === 0 && this.pendingBeholdHandCardIndices.length === 0) {
      this.beginBeholdSelection(card, graveyardIndex, true);
      return;
    }
    this.targetingCardIndex = graveyardIndex;
    this.targetingCardName = card.name;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
    this.selectingTarget = true;
    this.pendingTargetRequest = false;
    // Accept all permanents as potential targets — backend validates on cast
    const allIds = new Set<string>();
    const myBf = this.myBattlefieldFn();
    const opBf = this.opponentBattlefieldFn();
    for (const p of myBf) allIds.add(p.id);
    for (const p of opBf) allIds.add(p.id);
    this.validTargetIds.set(allIds);
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = 'Choose a target for ' + card.name + ' (flashback).';
  }

  startHarmonizeSelection(graveyardIndex: number, card: Card): void {
    this.pendingFlashback = true;
    this.harmonizing = true;
    this.harmonizeCardIndex = graveyardIndex;
    this.harmonizeCardName = card.name;
    this.harmonizeSelectedPermanentId.set(null);
    this.pendingHarmonizeCard = card;
  }

  toggleHarmonizeCreature(permanentId: string): void {
    if (!this.harmonizing) return;
    this.harmonizeSelectedPermanentId.set(
      this.harmonizeSelectedPermanentId() === permanentId ? null : permanentId);
  }

  isHarmonizeSelected(permanentId: string): boolean {
    return this.harmonizeSelectedPermanentId() === permanentId;
  }

  confirmHarmonize(): void {
    if (!this.harmonizing) return;
    const card = this.pendingHarmonizeCard;
    const cardIndex = this.harmonizeCardIndex;
    this.pendingHarmonizePermanentId = this.harmonizeSelectedPermanentId();
    this.clearHarmonizeState();
    if (card?.needsTarget) {
      this.continueFlashbackPlay(cardIndex, card);
    } else {
      this.sendPlayCardMessage(cardIndex, null);
    }
  }

  skipHarmonize(): void {
    if (!this.harmonizing) return;
    const card = this.pendingHarmonizeCard;
    const cardIndex = this.harmonizeCardIndex;
    this.pendingHarmonizePermanentId = null;
    this.clearHarmonizeState();
    if (card?.needsTarget) {
      this.continueFlashbackPlay(cardIndex, card);
    } else {
      this.sendPlayCardMessage(cardIndex, null);
    }
  }

  cancelHarmonize(): void {
    if (!this.harmonizing) return;
    this.clearHarmonizeState();
    this.pendingFlashback = false;
    this.pendingHarmonizePermanentId = null;
  }

  private clearHarmonizeState(): void {
    this.harmonizing = false;
    this.harmonizeCardIndex = -1;
    this.harmonizeCardName = '';
    this.harmonizeSelectedPermanentId.set(null);
    this.pendingHarmonizeCard = null;
  }

  // ========== Casting from exile / top of library ==========

  /** Cast a card the server marked playable from exile (impulse draw, prepare
      spells, ExileCast cards). The PLAY_CARD message identifies the card by
      fromExileCardId, so its cardIndex is unused and sent as 0. */
  startExilePlay(card: Card): void {
    if (!card.id) return;
    this.pendingFromExileCardId = card.id;
    this.pendingFromLibraryTop = false;
    this.continueZonePlay(card);
  }

  /** Cast the top card of the library (AllowCastFromTopOfLibraryEffect). */
  startLibraryTopPlay(card: Card): void {
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = true;
    this.continueZonePlay(card);
  }

  private continueZonePlay(card: Card): void {
    if ((card.exileCastCounterCost ?? 0) > 0
        && !this.selectingExileCounterCost
        && this.pendingExileCounterCostPermanentIds.length === 0) {
      this.pendingZoneCard = card;
      this.selectingExileCounterCost = true;
      this.exileCounterCostCardName = card.name;
      this.exileCounterCostRequired = card.exileCastCounterCost;
      this.exileCounterCostSelectedIds.set([]);
      return;
    }
    // Modal ("choose one/two") spell — pick mode(s) before anything else
    if (card.modalChoicesRequired > 0 && card.modalOptions && card.modalOptions.length > 0) {
      this.pendingZoneCard = card;
      this.choosingMode = true;
      this.modeCardIndex = 0;
      this.modeCardName = card.name;
      this.modeOptions = card.modalOptions;
      this.modeChoicesRequired = card.modalChoicesRequired;
      this.modeChoicesMax = card.modalChoicesMax > 0 ? card.modalChoicesMax : card.modalChoicesRequired;
      this.modeOptional = card.modalOptional;
      this.modeSelectedIndices = [];
      return;
    }

    if (card.requiresXValue === true) {
      this.pendingZoneCard = card;
      this.choosingXValue = true;
      this.xValueCardIndex = 0;
      this.xValueCardName = card.name;
      this.xValueInput = card.xValueMin ?? 0;
      this.xValueMaximum = card.xValueMax ?? 0;
      return;
    }

    const hasXCost = card.manaCost?.includes('{X}') ?? false;
    if (hasXCost) {
      const baseCost = (card.manaCost ?? '').replace('{X}', '');
      let base = 0;
      const matches = baseCost.match(/\{([^}]+)\}/g) || [];
      for (const m of matches) {
        const inner = m.slice(1, -1);
        const num = parseInt(inner);
        base += isNaN(num) ? 1 : num;
      }
      this.pendingZoneCard = card;
      this.choosingXValue = true;
      this.xValueCardIndex = 0;
      this.xValueCardName = card.name;
      this.xValueInput = 0;
      this.xValueMaximum = this.totalManaFn() - base;
      return;
    }
    if (card.needsSpellTarget) {
      this.targetingSpell = true;
      this.targetingSpellCardIndex = 0;
      this.targetingSpellCardName = card.name;
      return;
    }
    if (card.needsTarget) {
      this.enterZoneTargeting(card, null);
      return;
    }
    this.sendPlayCardMessage(0, null);
  }

  /** Like flashback targeting: the VALID_TARGETS_REQUEST handler only knows
      hand cards and abilities, so offer every permanent and player and let the
      engine's on-resolution legality check fizzle illegal choices. */
  private enterZoneTargeting(card: Card, xValue: number | null): void {
    this.selectingTarget = true;
    this.targetingCardIndex = 0;
    this.targetingCardName = card.name;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingAbilityXValue = xValue;
    this.pendingConvokeCard = null;
    const allIds = new Set<string>();
    for (const p of this.myBattlefieldFn()) allIds.add(p.id);
    for (const p of this.opponentBattlefieldFn()) allIds.add(p.id);
    this.validTargetIds.set(allIds);
    const g = this.gameSignal();
    this.validTargetPlayerIds.set(new Set(g?.playerIds ?? []));
    this.targetingPrompt = 'Choose a target for ' + card.name + '.';
  }

  private enterBestowTargeting(card: Card, cardIndex: number): void {
    this.selectingTarget = true;
    this.targetingCardIndex = cardIndex;
    this.targetingCardName = card.name;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
    const creatureIds = new Set<string>();
    for (const p of this.myBattlefieldFn()) {
      if (isPermanentCreature(p)) creatureIds.add(p.id);
    }
    for (const p of this.opponentBattlefieldFn()) {
      if (isPermanentCreature(p)) creatureIds.add(p.id);
    }
    this.validTargetIds.set(creatureIds);
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = 'Choose a creature for ' + card.name + ' to enchant.';
  }

  private sendPlayCardMessage(cardIndex: number, targetId: string | null, extra?: Record<string, any>): void {
    const msg: any = {
      type: MessageType.PLAY_CARD,
      cardIndex,
      targetId
    };
    if (this.pendingAlternateExileHandIndex != null) {
      const alternateHandCardIndices = [this.pendingAlternateExileHandIndex,
        ...this.pendingAlternateHandCardIndices];
      if (this.pendingAlternateHandCardDiscards || this.alternateCostDiscardsHandCard) {
        msg.sharedColorDiscardHandCardIndex = alternateHandCardIndices[0];
        if (alternateHandCardIndices.length > 1) {
          msg.discardHandCardIndices = alternateHandCardIndices.slice(1);
        }
      } else {
        msg.discardHandCardIndex = alternateHandCardIndices[0];
        if (this.pendingAlternateExileHandIndices.length > 1) {
          msg.discardHandCardIndices = this.pendingAlternateExileHandIndices;
        }
      }
      if (this.gameSignal()?.hand?.[cardIndex]?.keywords?.includes('MORPH')) {
        msg.morph = true;
      }
      this.pendingAlternateExileHandIndex = null;
      this.pendingAlternateHandCardIndices = [];
      this.pendingAlternateHandCardDiscards = false;
      this.pendingAlternateExileHandIndices = [];
    }
    if (this.pendingGraveyardCastDiscardHandIndex != null) {
      msg.discardHandCardIndex = this.pendingGraveyardCastDiscardHandIndex;
      this.pendingGraveyardCastDiscardHandIndex = null;
    }
    if (this.pendingBeholdPermanentId != null) {
      msg.beholdPermanentId = this.pendingBeholdPermanentId;
      this.pendingBeholdPermanentId = null;
    }
    if (this.pendingBeholdHandCardIndex != null) {
      msg.beholdHandCardIndex = this.pendingBeholdHandCardIndex;
      this.pendingBeholdHandCardIndex = null;
    }
    if (this.pendingBeholdPermanentIds.length > 0) {
      msg.beholdPermanentIds = this.pendingBeholdPermanentIds;
      this.pendingBeholdPermanentIds = [];
    }
    if (this.pendingBeholdHandCardIndices.length > 0) {
      msg.beholdHandCardIndices = this.pendingBeholdHandCardIndices;
      this.pendingBeholdHandCardIndices = [];
    }
    if (this.pendingBeholdChosenType != null) {
      msg.beholdCreatureType = this.pendingBeholdChosenType;
      this.pendingBeholdChosenType = null;
    }
    if (this.pendingPhyrexianLifeCount != null) {
      msg.phyrexianLifeCount = this.pendingPhyrexianLifeCount;
    }
    if (this.pendingFlashback) {
      msg.flashback = true;
      this.pendingFlashback = false;
    }
    if (this.pendingHarmonizePermanentId != null) {
      msg.alternateCostSacrificePermanentIds = [this.pendingHarmonizePermanentId];
      this.pendingHarmonizePermanentId = null;
    }
    if (this.pendingFromExileCardId != null) {
      msg.fromExileCardId = this.pendingFromExileCardId;
      this.pendingFromExileCardId = null;
    }
    if (this.pendingFromLibraryTop) {
      msg.fromLibraryTop = true;
      this.pendingFromLibraryTop = false;
    }
    if (this.pendingExileCounterCostPermanentIds.length > 0) {
      msg.exileCounterCostPermanentIds = this.pendingExileCounterCostPermanentIds;
      this.pendingExileCounterCostPermanentIds = [];
    }
    this.pendingZoneCard = null;
    if (this.pendingKicked) {
      msg.kicked = true;
      this.pendingKicked = false;
    }
    if (this.pendingKickerPermanentId != null) {
      msg.sacrificePermanentId = this.pendingKickerPermanentId;
      this.pendingKickerPermanentId = null;
    }
    if (this.pendingBuyback) {
      msg.buyback = true;
      this.pendingBuyback = false;
    }
    if (this.pendingBuybackDiscardHandIndices != null) {
      msg.discardHandCardIndices = this.pendingBuybackDiscardHandIndices;
      this.pendingBuybackDiscardHandIndices = null;
    }
    if (this.pendingBuybackSacrificePermanentId != null) {
      msg.sacrificePermanentId = this.pendingBuybackSacrificePermanentId;
      this.pendingBuybackSacrificePermanentId = null;
    }
    if (this.pendingBuybackSacrificePermanentIds.length > 0) {
      msg.additionalCostSacrificePermanentIds = this.pendingBuybackSacrificePermanentIds;
      this.pendingBuybackSacrificePermanentIds = [];
    }
    if (extra) {
      Object.assign(msg, extra);
    }
    this.pendingPhyrexianLifeCount = null;

    // MTGO-style casting: a plain hand cast whose cost the pool doesn't cover yet is
    // held back while the player taps mana sources; it is sent automatically once the
    // pool covers it (see onGameStateUpdate). Zone plays (flashback/exile/library-top)
    // keep the immediate path — the server marked them strictly affordable.
    const isZonePlay = msg.flashback || msg.fromExileCardId != null || msg.fromLibraryTop
        || msg.discardHandCardIndex != null
        || msg.sharedColorDiscardHandCardIndex != null
        || msg.morph === true
        || (msg.alternateCostSacrificePermanentIds?.length ?? 0) > 0;
    if (!isZonePlay && this.beginCastPaymentIfUnaffordable(msg)) {
      return;
    }
    this.websocketService.send(msg);
  }

  // ========== MTGO-style cast payment ==========

  /** Enters payment mode for the given PLAY_CARD message when its mana cost isn't
      covered by the current pool. Returns true when the message was held back. */
  private beginCastPaymentIfUnaffordable(msg: any): boolean {
    const g = this.gameSignal();
    const card = g?.hand?.[msg.cardIndex];
    if (!card) return false;

    const hasX = card.manaCost?.includes('{X}') ?? false;
    // msg.xValue doubles as the modal-mode encoding for non-X cards — only treat it as
    // generic mana when the card really has {X} in its cost.
    const xGeneric = hasX && typeof msg.xValue === 'number' && msg.xValue > 0 ? msg.xValue : 0;
    // The server's strict playability already prices in every cost modifier for the base
    // cost, so a client-side pool check is only needed for what strict playability doesn't
    // know: the announced X and a confirmed kicker. Checking the base cost here too would
    // strand cost-reduced cards in payment mode.
    let clientCheckedCost: string | null = null;
    if (xGeneric > 0) {
      clientCheckedCost = card.manaCost ?? '';
    }
    if (msg.kicked && card.kickerCost) {
      clientCheckedCost = (clientCheckedCost ?? card.manaCost ?? '') + card.kickerCost;
    }
    if (msg.buyback && card.buybackCost && !card.buybackRequiresSacrifice
        && !(card.buybackDiscardCount && card.buybackDiscardCount > 0)) {
      clientCheckedCost = (clientCheckedCost ?? card.manaCost ?? '') + card.buybackCost;
    }

    if (this.isStrictlyPlayableFn(msg.cardIndex)
        && (clientCheckedCost == null || this.canPayManaCost(clientCheckedCost, xGeneric))) {
      return false;
    }

    this.payingForCast = true;
    this.pendingCastCardIndex = msg.cardIndex;
    this.pendingCastCardName = card.name;
    this.pendingCastCardId = card.id ?? null;
    this.pendingCastManaCost = clientCheckedCost;
    this.pendingCastXValue = xGeneric;
    this.pendingCastMessage = msg;
    return true;
  }

  /** Called after every GAME_STATE while paying: sends the held-back cast/activation once
      the pool covers it, or abandons payment mode when the game moved on under us. */
  onGameStateUpdate(): void {
    this.onCastPaymentGameState();
    this.onAbilityPaymentGameState();
  }

  private onCastPaymentGameState(): void {
    if (!this.payingForCast) return;
    const g = this.gameSignal();
    const card = g?.hand?.[this.pendingCastCardIndex];
    const cardChanged = !card || (this.pendingCastCardId != null && card.id !== this.pendingCastCardId);
    if (!g || cardChanged || this.priorityMovedOn) {
      this.clearCastPayment();
      return;
    }
    if (this.isStrictlyPlayableFn(this.pendingCastCardIndex)
        && (this.pendingCastManaCost == null
            || this.canPayManaCost(this.pendingCastManaCost, this.pendingCastXValue))) {
      const msg = this.pendingCastMessage;
      this.clearCastPayment();
      this.websocketService.send(msg);
    }
  }

  /**
   * What the held-back message is waiting on, sent alongside every mana source tapped while
   * paying. The server has no payment session of its own, so this is the only way an "add one
   * mana of any colour" prompt can tell which colours would strand the payment and grey out the
   * rest. Advisory only — every colour stays answerable.
   *
   * Kicker and buyback casts are deliberately omitted: the server prices the card's own cost, so
   * an unmentioned optional cost could grey out a colour the player actually needs. Omitting the
   * intent just leaves all colours enabled.
   */
  private paymentIntent(): { handCardIndex?: number; xValue?: number; abilityPermanentId?: string; abilityIndex?: number } | undefined {
    if (this.payingForCast && this.pendingCastCardIndex >= 0) {
      const msg = this.pendingCastMessage;
      if (msg?.kicked || msg?.buyback) return undefined;
      return { handCardIndex: this.pendingCastCardIndex, xValue: this.pendingCastXValue };
    }
    if (this.payingForAbility && this.pendingActivationPermanentId != null) {
      const abilityIndex = this.pendingActivationMessage?.abilityIndex;
      if (typeof abilityIndex !== 'number') return undefined;
      return { abilityPermanentId: this.pendingActivationPermanentId, abilityIndex };
    }
    return undefined;
  }

  /** Sends a TAP_PERMANENT, tagged with the payment it is serving when one is in progress. */
  private sendTapPermanent(permanentIndex: number): void {
    this.websocketService.send({
      type: MessageType.TAP_PERMANENT,
      permanentIndex,
      paymentIntent: this.paymentIntent()
    });
  }

  /** Cancel button / Esc while paying: drop the pending cast and untap the mana
      sources tapped for it (the server reverts the recorded mana activations). */
  cancelPendingCast(): void {
    if (!this.payingForCast) return;
    this.clearCastPayment();
    this.websocketService.send({ type: MessageType.REVERT_MANA_ACTIVATIONS });
  }

  private clearCastPayment(): void {
    this.payingForCast = false;
    this.pendingCastCardIndex = -1;
    this.pendingCastCardName = '';
    this.pendingCastCardId = null;
    this.pendingCastManaCost = null;
    this.pendingCastXValue = 0;
    this.pendingCastMessage = null;
  }

  // ========== MTGO-style ability activation payment ==========

  /** Sends an ACTIVATE_ABILITY message, or holds it back in payment mode when its mana
      cost isn't covered by the current pool (mirroring sendPlayCardMessage for casts). */
  private sendActivateAbilityMessage(msg: any): void {
    // Read the intent before the call below can start a payment of its own, so a mana ability
    // activated to pay for something is tagged with what it is paying for — not with itself.
    const intent = this.paymentIntent();
    if (this.beginAbilityPaymentIfUnaffordable(msg)) return;
    this.websocketService.send(intent ? { ...msg, paymentIntent: intent } : msg);
  }

  /** Enters payment mode for the given ACTIVATE_ABILITY message when its mana cost isn't
      covered by the current pool. Returns true when the message was held back. */
  private beginAbilityPaymentIfUnaffordable(msg: any): boolean {
    // Never stack a second payment on one already in progress — while paying, clicks are
    // restricted to strictly affordable mana production, so this is just a safety net.
    if (this.payingForCast || this.payingForAbility) return false;
    const perm = this.myBattlefieldFn()[msg.permanentIndex];
    const ability = perm?.card.activatedAbilities?.[msg.abilityIndex];
    // Loyalty and tap/sacrifice-only costs have no mana component to pay for.
    if (!perm || !ability?.manaCost) return false;
    const hasX = ability.manaCost.includes('{X}');
    const xGeneric = hasX && typeof msg.xValue === 'number' && msg.xValue > 0 ? msg.xValue : 0;
    if (this.canPayManaCost(ability.manaCost, xGeneric)) return false;

    this.payingForAbility = true;
    this.pendingActivationSourceName = perm.card.name;
    this.pendingActivationPermanentId = perm.id;
    this.pendingActivationManaCost = ability.manaCost;
    this.pendingActivationXValue = xGeneric;
    this.pendingActivationRequiresTap = ability.requiresTap;
    this.pendingActivationMessage = msg;
    return true;
  }

  private onAbilityPaymentGameState(): void {
    if (!this.payingForAbility) return;
    const g = this.gameSignal();
    const index = this.myBattlefieldFn().findIndex(p => p.id === this.pendingActivationPermanentId);
    if (!g || index < 0 || this.priorityMovedOn) {
      this.clearAbilityPayment();
      return;
    }
    if (this.pendingActivationManaCost != null
        && this.canPayManaCost(this.pendingActivationManaCost, this.pendingActivationXValue)) {
      const msg = this.pendingActivationMessage;
      msg.permanentIndex = index; // battlefield order may have changed while paying
      this.clearAbilityPayment();
      this.websocketService.send(msg);
    }
  }

  /** Cancel button / Esc while paying: drop the pending activation and untap the mana
      sources tapped for it (the server reverts the recorded mana activations). */
  cancelPendingAbility(): void {
    if (!this.payingForAbility) return;
    this.clearAbilityPayment();
    this.websocketService.send({ type: MessageType.REVERT_MANA_ACTIVATIONS });
  }

  private clearAbilityPayment(): void {
    this.payingForAbility = false;
    this.pendingActivationSourceName = '';
    this.pendingActivationPermanentId = null;
    this.pendingActivationManaCost = null;
    this.pendingActivationXValue = 0;
    this.pendingActivationRequiresTap = false;
    this.pendingActivationMessage = null;
  }

  /** Open targeting for a graveyard activated ability that needs a battlefield target (e.g. Gryff's Boon). */
  startGraveyardAbilityTargeting(graveyardCardIndex: number, ability: ActivatedAbilityView): void {
    this.selectingTarget = true;
    this.targetingCardIndex = graveyardCardIndex;
    this.targetingCardName = ability.description ?? 'Ability';
    this.targetingForAbility = false;
    this.targetingForGraveyardAbility = true;
    this.targetingAbilityIndex = 0;
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
    if (ability.maxTargets > 1) {
      // Target group on the battlefield/players (Soul of Shandalar) — the backend enumerates each
      // position, exactly as it does for a battlefield ability.
      this.selectingTarget = false;
      this.sendValidTargetsRequest(null, null, this.targetingAbilityIndex, [], null, graveyardCardIndex);
      return;
    }
    const allIds = new Set<string>();
    for (const p of this.myBattlefieldFn()) allIds.add(p.id);
    for (const p of this.opponentBattlefieldFn()) allIds.add(p.id);
    this.validTargetIds.set(allIds);
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = 'Choose a target for ' + (ability.description ?? 'the ability') + '.';
  }

  /** Open the X prompt for a graveyard activated ability whose cost contains {X} (e.g. Evershrike). */
  startGraveyardXValue(graveyardCardIndex: number, ability: ActivatedAbilityView): void {
    let base = 0;
    const baseCost = (ability.manaCost ?? '').replace('{X}', '');
    const matches = baseCost.match(/\{([^}]+)\}/g) || [];
    for (const m of matches) {
      const inner = m.slice(1, -1);
      const num = parseInt(inner);
      base += isNaN(num) ? 1 : num;
    }
    this.graveyardXCardIndex = graveyardCardIndex;
    this.choosingXValue = true;
    this.xValueCardIndex = -1;
    this.xValueCardName = ability.description ?? 'Ability';
    this.xValueInput = 0;
    this.xValueMaximum = Math.max(0, this.totalManaFn() - base);
  }

  confirmXValue(): void {
    const g = this.gameSignal();
    if (!g) return;

    if (this.graveyardXCardIndex >= 0) {
      this.websocketService.send({
        type: MessageType.ACTIVATE_GRAVEYARD_ABILITY,
        graveyardCardIndex: this.graveyardXCardIndex,
        abilityIndex: 0,
        xValue: this.xValueInput
      });
      this.choosingXValue = false;
      this.graveyardXCardIndex = -1;
      this.xValueCardName = '';
      this.xValueInput = 0;
      return;
    }

    if (this.targetingForAbility) {
      const perm = this.myBattlefieldFn()[this.xValueCardIndex];
      const ability = perm?.card.activatedAbilities[this.targetingAbilityIndex];
      if (ability?.needsTarget) {
        // Store X value and request valid targets from backend
        this.pendingAbilityXValue = this.xValueInput;
        this.choosingXValue = false;
        this.targetingCardIndex = this.xValueCardIndex;
        this.targetingCardName = this.xValueCardName;
        this.sendValidTargetsRequest(null, this.xValueCardIndex, this.targetingAbilityIndex,
          [], this.pendingAbilityXValue);
        return;
      }
      // X value only, no target
      this.sendActivateAbilityMessage({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.xValueCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        xValue: this.xValueInput
      });
    } else {
      const card = this.pendingZoneCard ?? g.hand[this.xValueCardIndex];
      if (this.pendingZoneCard && card?.needsTarget) {
        // Exile / library-top cast with X and a target — enter manual targeting
        const zoneCard = this.pendingZoneCard;
        const savedXValue = this.xValueInput;
        this.pendingZoneCard = null;
        this.choosingXValue = false;
        this.xValueCardIndex = -1;
        this.xValueCardName = '';
        this.enterZoneTargeting(zoneCard, savedXValue);
        return;
      }
      if (card?.needsTarget) {
        // Store X value and request valid targets from backend
        const savedXValue = this.xValueInput;
        const savedCardIndex = this.xValueCardIndex;
        const savedCardName = this.xValueCardName;
        this.choosingXValue = false;
        this.targetingCardIndex = savedCardIndex;
        this.targetingCardName = savedCardName;
        this.targetingForAbility = false;
        this.targetingAbilityIndex = -1;
        this.pendingAbilityXValue = savedXValue;
        this.pendingConvokeCard = null;
        this.xValueCardIndex = -1;
        this.xValueCardName = '';
        this.sendValidTargetsRequest(savedCardIndex, null, null, [], savedXValue);
        return;
      }
      this.sendPlayCardMessage(this.xValueCardIndex, null, { xValue: this.xValueInput });
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
    this.graveyardXCardIndex = -1;
    this.xValueCardName = '';
    this.xValueInput = 0;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingPhyrexianLifeCount = null;
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
    this.pendingExileCounterCostPermanentIds = [];
    this.pendingBeholdPermanentId = null;
    this.pendingBeholdHandCardIndex = null;
    this.pendingBeholdPermanentIds = [];
    this.pendingBeholdHandCardIndices = [];
  }

  selectTarget(permanentId: string): void {
    if (!this.selectingTarget) return;
    if (!this.validTargetIds().has(permanentId)) return;
    if (this.targetingForGraveyardAbility) {
      const msg: any = {
        type: MessageType.ACTIVATE_GRAVEYARD_ABILITY,
        graveyardCardIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex >= 0 ? this.targetingAbilityIndex : 0,
        targetId: permanentId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.websocketService.send(msg);
    } else if (this.targetingForAbility) {
      const msg: any = {
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetId: permanentId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.sendActivateAbilityMessage(msg);
    } else if (this.pendingConvokeCard?.hasConvoke) {
      // Single-target spell with convoke — save target and enter convoke mode.
      // Preserve a pending X value / mode selection across the targeting-state reset.
      const cardIndex = this.targetingCardIndex;
      const card = this.pendingConvokeCard;
      const savedXValue = this.pendingAbilityXValue;
      this.pendingMultiTargetIds = [permanentId];
      this.resetTargetingState();
      this.pendingAbilityXValue = savedXValue;
      this.enterConvokeMode(cardIndex, card);
      return;
    } else {
      const extra: Record<string, any> = {};
      if (this.pendingAbilityXValue != null) {
        extra['xValue'] = this.pendingAbilityXValue;
      }
      this.sendPlayCardMessage(this.targetingCardIndex, permanentId, extra);
    }
    this.resetTargetingState();
  }

  selectPlayerTarget(playerIndex: number): void {
    if (!this.selectingTarget) return;
    const g = this.gameSignal();
    if (!g) return;
    const playerId = g.playerIds[playerIndex];
    if (!this.validTargetPlayerIds().has(playerId)) return;
    if (this.targetingForAbility) {
      const msg: any = {
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetId: playerId
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.sendActivateAbilityMessage(msg);
    } else {
      const extra: Record<string, any> = {};
      if (this.pendingAbilityXValue != null) {
        extra['xValue'] = this.pendingAbilityXValue;
      }
      this.sendPlayCardMessage(this.targetingCardIndex, playerId, extra);
    }
    this.resetTargetingState();
  }

  selectGraveyardTarget(cardId: string): void {
    if (!this.targetingGraveyard) return;
    if (!this.graveyardTargetCardIds.includes(cardId)) return;
    if (this.multiTargeting) {
      const current = this.multiTargetSelectedIds();
      if (current.includes(cardId) || current.length >= this.multiTargetMaxCount) return;
      const newSelected = [...current, cardId];
      this.targetingGraveyard = false;
      this.graveyardTargetCards = [];
      this.graveyardTargetCardIds = [];
      this.graveyardTargetPrompt = '';
      this.multiTargetSelectedIds.set(newSelected);
      if (newSelected.length < this.multiTargetMaxCount) {
        this.refreshMultiTargets(newSelected);
      } else {
        this.confirmMultiTargets();
      }
      return;
    }
    const extra: Record<string, any> = {};
    if (this.pendingAbilityXValue != null) {
      extra['xValue'] = this.pendingAbilityXValue;
    }
    this.sendPlayCardMessage(this.targetingCardIndex, cardId, extra);
    this.targetingGraveyard = false;
    this.graveyardTargetCards = [];
    this.graveyardTargetCardIds = [];
    this.graveyardTargetPrompt = '';
    this.resetTargetingState();
  }

  selectExileTarget(cardId: string): void {
    if (!this.targetingExile) return;
    if (!this.exileTargetCardIds.includes(cardId)) return;
    if (this.targetingForAbility) {
      const msg: any = {
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetId: cardId,
        targetZone: 'EXILE'
      };
      if (this.pendingAbilityXValue != null) {
        msg.xValue = this.pendingAbilityXValue;
      }
      this.sendActivateAbilityMessage(msg);
    }
    this.targetingExile = false;
    this.exileTargetCards = [];
    this.exileTargetCardIds = [];
    this.exileTargetPrompt = '';
    this.resetTargetingState();
  }

  cancelExileTargeting(): void {
    this.targetingExile = false;
    this.exileTargetCards = [];
    this.exileTargetCardIds = [];
    this.exileTargetPrompt = '';
    this.resetTargetingState();
  }

  cancelGraveyardTargeting(): void {
    const wasMultiTargeting = this.multiTargeting;
    this.targetingGraveyard = false;
    this.graveyardTargetCards = [];
    this.graveyardTargetCardIds = [];
    this.graveyardTargetPrompt = '';
    if (wasMultiTargeting) {
      this.cancelMultiTargeting();
    }
    this.resetTargetingState();
  }

  private resetTargetingState(): void {
    this.selectingTarget = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingForGraveyardAbility = false;
    this.targetingAbilityIndex = -1;
    this.validTargetIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = '';
    this.targetingExile = false;
    this.exileTargetCards = [];
    this.exileTargetCardIds = [];
    this.exileTargetPrompt = '';
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
    // A completed cast consumes these in sendPlayCardMessage before we get here;
    // clearing them covers the cancel paths so the flags can't leak into a later cast.
    this.pendingFlashback = false;
    this.pendingHarmonizePermanentId = null;
    this.selectingGraveyardCastDiscard = false;
    this.graveyardCastDiscardCardIndex = -1;
    this.graveyardCastDiscardCardName = '';
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
    this.pendingExileCounterCostPermanentIds = [];
    // Note: don't reset pendingPhyrexianLifeCount here — it carries through to the final send
  }

  cancelTargeting(): void {
    this.resetTargetingState();
    this.pendingGraveyardCastDiscardHandIndex = null;
    this.pendingPhyrexianLifeCount = null;
  }

  selectSpellTarget(entry: StackEntry): void {
    if (!this.targetingSpell || !entry.isSpell) return;
    if (this.targetingForAbility) {
      this.sendActivateAbilityMessage({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingSpellCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetId: entry.cardId
      });
    } else if (this.spellTargetCount > 1) {
      // Modal mode targeting several spells (one per declared target slot, in card-text order)
      if (this.spellTargetSelectedIds.includes(entry.cardId)) return;
      this.spellTargetSelectedIds = [...this.spellTargetSelectedIds, entry.cardId];
      if (this.spellTargetSelectedIds.length < this.spellTargetCount) return;
      const extra: Record<string, any> = { targetIds: this.spellTargetSelectedIds };
      if (this.pendingAbilityXValue != null) {
        extra['xValue'] = this.pendingAbilityXValue;
      }
      this.sendPlayCardMessage(this.targetingSpellCardIndex, null, extra);
    } else {
      const extra: Record<string, any> = {};
      if (this.pendingAbilityXValue != null) {
        extra['xValue'] = this.pendingAbilityXValue;
      }
      this.sendPlayCardMessage(this.targetingSpellCardIndex, entry.cardId, extra);
    }
    this.resetSpellTargetingState();
  }

  cancelSpellTargeting(): void {
    this.resetSpellTargetingState();
    this.pendingPhyrexianLifeCount = null;
  }

  private resetSpellTargetingState(): void {
    this.targetingSpell = false;
    this.targetingSpellCardIndex = -1;
    this.targetingSpellCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingAbilityXValue = null;
    this.spellTargetCount = 1;
    this.spellTargetSelectedIds = [];
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
  }

  isValidTarget(perm: Permanent): boolean {
    return this.validTargetIds().has(perm.id);
  }

  // ========== Multi-target selection ==========

  addMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    const current = this.multiTargetSelectedIds();
    if (current.includes(permanentId)) return;
    if (current.length >= this.multiTargetMaxCount) return;
    if (!this.validTargetIds().has(permanentId)) return;
    const newSelected = [...current, permanentId];
    this.multiTargetSelectedIds.set(newSelected);
    // Refresh valid targets for next position
    if (newSelected.length < this.multiTargetMaxCount) {
      this.refreshMultiTargets(newSelected);
    }
  }

  addMultiTargetPlayer(playerIndex: number): void {
    if (!this.multiTargeting) return;
    const g = this.gameSignal();
    if (!g) return;
    const playerId = g.playerIds[playerIndex];
    if (!this.validTargetPlayerIds().has(playerId)) return;
    const current = this.multiTargetSelectedIds();
    if (current.includes(playerId)) return;
    if (current.length >= this.multiTargetMaxCount) return;
    const newSelected = [...current, playerId];
    this.multiTargetSelectedIds.set(newSelected);
    // Refresh valid targets for next position
    if (newSelected.length < this.multiTargetMaxCount) {
      this.refreshMultiTargets(newSelected);
    }
  }

  removeMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    const newSelected = this.multiTargetSelectedIds().filter(id => id !== permanentId);
    this.multiTargetSelectedIds.set(newSelected);
    // Request refreshed valid targets from backend with updated already-selected list
    this.refreshMultiTargets(newSelected);
  }

  /** Re-enumerates the next position's legal targets for whichever zone the ability lives in. */
  private refreshMultiTargets(selected: string[]): void {
    if (this.targetingForGraveyardAbility) {
      this.sendValidTargetsRequest(null, null, this.targetingAbilityIndex, selected, null, this.multiTargetCardIndex);
    } else if (this.targetingForAbility) {
      this.sendValidTargetsRequest(null, this.multiTargetCardIndex, this.targetingAbilityIndex, selected);
    } else {
      this.sendValidTargetsRequest(this.multiTargetCardIndex, null, null, selected);
    }
  }

  confirmMultiTargets(): void {
    if (!this.multiTargeting) return;
    const selected = this.multiTargetSelectedIds();
    if (selected.length < this.multiTargetMinCount) return;

    const card = this.pendingConvokeCard;
    this.pendingMultiTargetIds = [...selected];

    this.multiTargeting = false;
    this.multiTargetSelectedIds.set([]);
    this.validTargetIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());

    if (this.targetingForGraveyardAbility) {
      // Multi-target graveyard activated ability (Soul of Shandalar) — the targets ride in the
      // same list field the graveyard-card targeting flavour uses.
      this.websocketService.send({
        type: MessageType.ACTIVATE_GRAVEYARD_ABILITY,
        graveyardCardIndex: this.multiTargetCardIndex,
        abilityIndex: this.targetingAbilityIndex >= 0 ? this.targetingAbilityIndex : 0,
        graveyardCardIds: this.pendingMultiTargetIds
      });
      this.resetTargetingState();
      this.resetMultiTargetState();
      return;
    }

    if (this.targetingForAbility) {
      // Multi-target activated ability (e.g. Brass Squire)
      this.sendActivateAbilityMessage({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.multiTargetCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetIds: this.pendingMultiTargetIds
      });
      this.resetTargetingState();
      this.resetMultiTargetState();
      return;
    }

    // If card has convoke, enter convoke mode
    if (card?.hasConvoke) {
      this.enterConvokeMode(this.multiTargetCardIndex, card);
      return;
    }

    // Send directly
    const multiExtra: Record<string, any> = { targetIds: this.pendingMultiTargetIds };
    if (this.pendingAbilityXValue != null) {
      multiExtra['xValue'] = this.pendingAbilityXValue;
    }
    this.sendPlayCardMessage(this.multiTargetCardIndex, null, multiExtra);
    this.resetTargetingState();
    this.resetMultiTargetState();
  }

  cancelMultiTargeting(): void {
    this.multiTargeting = false;
    this.multiTargetCardIndex = -1;
    this.multiTargetCardName = '';
    this.multiTargetSelectedIds.set([]);
    this.validTargetIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());
    this.pendingConvokeCard = null;
    this.targetingForAbility = false;
    this.targetingForGraveyardAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingPhyrexianLifeCount = null;
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
    this.pendingConvokeCard = card;
    this.convokeSelectedCreatureIds.set([]);
  }

  canSelectCastingAssistance(permanent: Permanent): boolean {
    if (!this.convoking || !permanent || permanent.tapped) return false;
    const card = this.pendingConvokeCard;
    const canConvoke = card?.keywords.includes('CONVOKE') ?? false;
    const canImprovise = card?.keywords.includes('IMPROVISE') ?? false;
    return (canConvoke && isPermanentCreature(permanent))
      || (canImprovise && isPermanentArtifact(permanent));
  }

  castingAssistancePermanentLabel(): string {
    const card = this.pendingConvokeCard;
    const canConvoke = card?.keywords.includes('CONVOKE') ?? false;
    const canImprovise = card?.keywords.includes('IMPROVISE') ?? false;
    return canConvoke && canImprovise ? 'creatures or artifacts'
      : canImprovise ? 'artifacts' : 'creatures';
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
    this.addPendingPhyrexianToMsg(msg);
    this.addPendingKickedToMsg(msg);
    this.addPendingBuybackToMsg(msg);
    this.addPendingBuybackDiscardToMsg(msg);
    this.addPendingXValueToMsg(msg);
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
    this.addPendingPhyrexianToMsg(msg);
    this.addPendingKickedToMsg(msg);
    this.addPendingBuybackToMsg(msg);
    this.addPendingBuybackDiscardToMsg(msg);
    this.addPendingXValueToMsg(msg);
    this.websocketService.send(msg);
    this.cancelConvoke();
    this.resetMultiTargetState();
  }

  private addPendingTargetsToMsg(msg: any): void {
    if (this.pendingMultiTargetIds.length > 0) {
      if (this.pendingConvokeCard && this.multiTargetMaxCount > 1) {
        msg.targetIds = this.pendingMultiTargetIds;
      } else {
        // Single-target card that went through convoke flow
        msg.targetId = this.pendingMultiTargetIds[0];
      }
    }
  }

  private addPendingPhyrexianToMsg(msg: any): void {
    if (this.pendingPhyrexianLifeCount != null) {
      msg.phyrexianLifeCount = this.pendingPhyrexianLifeCount;
      this.pendingPhyrexianLifeCount = null;
    }
  }

  private addPendingKickedToMsg(msg: any): void {
    if (this.pendingKicked) {
      msg.kicked = true;
      this.pendingKicked = false;
    }
  }

  private addPendingBuybackToMsg(msg: any): void {
    if (this.pendingBuyback) {
      msg.buyback = true;
      this.pendingBuyback = false;
    }
  }

  private addPendingBuybackDiscardToMsg(msg: any): void {
    if (this.pendingBuybackDiscardHandIndices != null) {
      msg.discardHandCardIndices = this.pendingBuybackDiscardHandIndices;
      this.pendingBuybackDiscardHandIndices = null;
    }
  }

  private addPendingXValueToMsg(msg: any): void {
    if (this.pendingAbilityXValue != null) {
      msg.xValue = this.pendingAbilityXValue;
      this.pendingAbilityXValue = null;
    }
  }

  cancelConvoke(): void {
    this.convoking = false;
    this.convokeCardIndex = -1;
    this.convokeCardName = '';
    this.convokeSelectedCreatureIds.set([]);
    this.pendingPhyrexianLifeCount = null;
  }

  // ========== Alternate casting cost selection ==========

  private beginBeholdSelection(card: Card, cardIndex: number, fromGraveyard: boolean): void {
    this.choosingCreatureTypeOnly = card.additionalChooseCreatureType === true;
    this.beholdChosenCreatureType = card.additionalBeholdChosenCreatureType || this.choosingCreatureTypeOnly;
    this.choosingBehold = this.beholdChosenCreatureType || card.additionalBeholdCount <= 1;
    this.selectingBeholdPermanent = !this.beholdChosenCreatureType && card.additionalBeholdCount > 1;
    this.selectingBeholdHandCard = !this.beholdChosenCreatureType && card.additionalBeholdCount > 1;
    this.beholdCardIndex = cardIndex;
    this.beholdCardName = card.name;
    this.beholdSubtype = card.additionalBeholdSubtype ?? '';
    this.beholdRequiredCount = Math.max(1, card.additionalBeholdCount);
    this.beholdSelectedCount = 0;
    this.beholdCardIsInGraveyard = fromGraveyard;
    this.pendingBeholdCard = card;
    this.pendingBeholdIsFlashback = fromGraveyard;
    this.pendingBeholdPermanentIds = [];
    this.pendingBeholdHandCardIndices = [];
    this.pendingBeholdChosenType = null;
  }

  chooseBeholdType(chosenType: string): void {
    if (!this.choosingBehold || !this.beholdChosenCreatureType || !chosenType) return;
    this.beholdChosenType = chosenType;
    this.beholdSubtype = chosenType;
    this.pendingBeholdChosenType = chosenType;
    this.choosingBehold = false;
    if (this.choosingCreatureTypeOnly) {
      this.skipBeholdForCardIndex = this.beholdCardIndex;
      this.finishBeholdSelection(this.beholdCardIndex);
      return;
    }
    this.selectingBeholdPermanent = true;
    this.selectingBeholdHandCard = true;
  }

  get beholdCreatureTypes(): string[] {
    if (this.choosingCreatureTypeOnly) {
      return this.pendingBeholdCard?.additionalCreatureTypeChoices ?? [];
    }
    const counts = new Map<string, number>();
    const addCard = (card: Card): void => {
      if (card.type !== 'CREATURE' && !(card.additionalTypes ?? []).includes('CREATURE')) return;
      for (const subtype of card.subtypes ?? []) {
        counts.set(subtype, (counts.get(subtype) ?? 0) + 1);
      }
    };
    for (const permanent of this.myBattlefieldFn()) addCard(permanent.card);
    const game = this.gameSignal();
    if (game) {
      game.hand.forEach((card, index) => {
        if (index !== this.beholdCardIndex) addCard(card);
      });
    }
    return [...counts.entries()]
      .filter(([, count]) => count >= this.beholdRequiredCount)
      .map(([subtype]) => subtype)
      .sort();
  }

  chooseBeholdPermanent(): void {
    if (!this.choosingBehold) return;
    this.choosingBehold = false;
    this.selectingBeholdPermanent = true;
  }

  chooseBeholdHandCard(): void {
    if (!this.choosingBehold) return;
    this.choosingBehold = false;
    this.selectingBeholdHandCard = true;
  }

  selectBeholdPermanent(permanentId: string): void {
    if (!this.selectingBeholdPermanent) return;
    if (this.pendingBeholdPermanentIds.includes(permanentId)) return;
    this.pendingBeholdPermanentIds.push(permanentId);
    this.beholdSelectedCount = this.pendingBeholdPermanentIds.length + this.pendingBeholdHandCardIndices.length;
    if (this.beholdRequiredCount > 1) {
      if (this.beholdSelectedCount < this.beholdRequiredCount) return;
      this.selectingBeholdPermanent = false;
      this.selectingBeholdHandCard = false;
      this.finishBeholdSelection();
      return;
    }
    const spellIndex = this.beholdCardIndex;
    this.pendingBeholdPermanentId = permanentId;
    this.finishBeholdSelection(spellIndex);
  }

  selectBeholdHandCard(handIndex: number): void {
    if (!this.selectingBeholdHandCard
        || (!this.beholdCardIsInGraveyard && handIndex === this.beholdCardIndex)
        || this.pendingBeholdHandCardIndices.includes(handIndex)) return;
    this.pendingBeholdHandCardIndices.push(handIndex);
    this.beholdSelectedCount = this.pendingBeholdPermanentIds.length + this.pendingBeholdHandCardIndices.length;
    if (this.beholdRequiredCount > 1) {
      if (this.beholdSelectedCount < this.beholdRequiredCount) return;
      this.selectingBeholdPermanent = false;
      this.selectingBeholdHandCard = false;
      this.finishBeholdSelection();
      return;
    }
    const spellIndex = this.beholdCardIndex;
    this.pendingBeholdHandCardIndex = handIndex;
    this.finishBeholdSelection(spellIndex);
  }

  private finishBeholdSelection(singleSpellIndex?: number): void {
    const card = this.pendingBeholdCard;
    const spellIndex = singleSpellIndex ?? this.beholdCardIndex;
    const fromGraveyard = this.pendingBeholdIsFlashback;
    this.choosingBehold = false;
    this.selectingBeholdPermanent = false;
    this.selectingBeholdHandCard = false;
    this.beholdCardIndex = -1;
    this.beholdCardName = '';
    this.beholdSubtype = '';
    this.beholdChosenCreatureType = false;
    this.beholdChosenType = '';
    this.beholdRequiredCount = 1;
    this.beholdSelectedCount = 0;
    this.beholdCardIsInGraveyard = false;
    this.choosingCreatureTypeOnly = false;
    this.pendingBeholdCard = null;
    this.pendingBeholdIsFlashback = false;
    if (fromGraveyard && card) {
      this.continueFlashbackPlay(spellIndex, card);
    } else {
      this.continuePlayCard(spellIndex);
    }
  }

  cancelBehold(): void {
    this.choosingBehold = false;
    this.selectingBeholdPermanent = false;
    this.selectingBeholdHandCard = false;
    this.beholdCardIndex = -1;
    this.beholdCardName = '';
    this.beholdSubtype = '';
    this.beholdChosenCreatureType = false;
    this.beholdChosenType = '';
    this.beholdRequiredCount = 1;
    this.beholdSelectedCount = 0;
    this.beholdCardIsInGraveyard = false;
    this.pendingBeholdPermanentId = null;
    this.pendingBeholdHandCardIndex = null;
    this.pendingBeholdPermanentIds = [];
    this.pendingBeholdHandCardIndices = [];
    this.pendingBeholdChosenType = null;
    this.choosingCreatureTypeOnly = false;
    this.pendingBeholdCard = null;
    this.pendingBeholdIsFlashback = false;
  }

  declineBehold(): void {
    if (!this.choosingBehold || !this.beholdChosenCreatureType) return;
    const cardIndex = this.beholdCardIndex;
    const fromGraveyard = this.pendingBeholdIsFlashback;
    this.skipBeholdForCardIndex = cardIndex;
    this.cancelBehold();
    if (fromGraveyard) {
      const game = this.gameSignal();
      const playerIndex = game?.playerIds.indexOf(this.websocketService.currentUser?.userId ?? '') ?? -1;
      const card = playerIndex >= 0 ? game?.graveyards[playerIndex]?.[cardIndex] : undefined;
      if (card) this.continueFlashbackPlay(cardIndex, card);
    } else {
      this.continuePlayCard(cardIndex);
    }
  }

  choosePayMana(): void {
    const savedIndex = this.alternateCostCardIndex;
    this.resetAlternateCostState();
    this.continuePlayCard(savedIndex);
  }

  choosePayAlternateCost(): void {
    this.choosingAlternateCost = false;
    const battlefieldNeeded = this.alternateCostSacrificeCount + this.alternateCostTapCount + this.alternateCostReturnCount;
    if (battlefieldNeeded > 0) {
      this.selectingAlternateCostCreatures = true;
      this.alternateCostSelectedIds.set([]);
      return;
    }
    if (this.alternateCostExileHandCount > 0) {
      this.selectingAlternateCostHandCard = true;
      return;
    }
    if (this.alternateCostRequiresTarget) {
      const savedIndex = this.alternateCostCardIndex;
      const g = this.gameSignal();
      const card = g?.hand[savedIndex];
      this.resetAlternateCostState();
      if (card) {
        this.enterBestowTargeting(card, savedIndex);
      }
      return;
    }
    // Alternate cost with no permanent or hand payment
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.alternateCostCardIndex,
      alternateCostSacrificePermanentIds: [],
      morph: this.gameSignal()?.hand?.[this.alternateCostCardIndex]?.keywords?.includes('MORPH') ?? false
    });
    this.resetAlternateCostState();
  }

  /** Hand-card click while paying an exile- or discard-from-hand alternate casting cost. */
  selectAlternateCostHandCard(handIndex: number): void {
    if (!this.selectingAlternateCostHandCard) return;
    if (handIndex === this.alternateCostCardIndex) return;
    if (this.pendingAlternateHandCardIndices.includes(handIndex)) return;
    if (!this.alternateCostDiscardsHandCard && this.alternateCostExileHandCount > 1) {
      const selected = this.alternateCostSelectedHandIndices();
      if (selected.includes(handIndex)) {
        this.alternateCostSelectedHandIndices.set(selected.filter(index => index !== handIndex));
      } else if (selected.length < this.alternateCostExileHandCount) {
        this.alternateCostSelectedHandIndices.set([...selected, handIndex]);
      }
      return;
    }
    if (this.pendingAlternateHandCardIndices.includes(handIndex)) return;
    const spellIndex = this.alternateCostCardIndex;
    this.pendingAlternateHandCardIndices.push(handIndex);
    const requiredHandCardCount = this.alternateCostDiscardsHandCard
      ? Math.max(1, this.alternateCostExileHandCount) : 1;
    if (this.pendingAlternateHandCardIndices.length < requiredHandCardCount) return;
    this.pendingAlternateExileHandIndex = this.pendingAlternateHandCardIndices[0] ?? handIndex;
    this.pendingAlternateHandCardIndices = this.pendingAlternateHandCardIndices.slice(1);
    this.pendingAlternateHandCardDiscards = this.alternateCostDiscardsHandCard;
    this.selectingAlternateCostHandCard = false;
    this.choosingAlternateCost = false;
    this.selectingAlternateCostCreatures = false;
    // Keep spell index; clear other alternate UI state but preserve pending exile index.
    this.alternateCostCardIndex = -1;
    this.alternateCostCardName = '';
    this.alternateCostSacrificeCount = 0;
    this.alternateCostLifePayment = 0;
    this.alternateCostTapCount = 0;
    this.alternateCostReturnCount = 0;
    this.alternateCostManaCost = '';
    this.alternateCostExileHandCount = 0;
    this.alternateCostExileHandLabel = '';
    this.alternateCostRevealsHandCard = false;
    this.alternateCostDiscardsHandCard = false;
    this.alternateCostRequiresTarget = false;
    this.alternateCostSelectedIds.set([]);
    this.continuePlayCard(spellIndex);
  }

  isAlternateCostHandCardSelected(handIndex: number): boolean {
    return this.alternateCostSelectedHandIndices().includes(handIndex);
  }

  confirmAlternateCostHandCards(): void {
    if (!this.selectingAlternateCostHandCard || this.alternateCostExileHandCount <= 1) return;
    const selected = this.alternateCostSelectedHandIndices();
    if (selected.length !== this.alternateCostExileHandCount) return;
    const spellIndex = this.alternateCostCardIndex;
    this.resetAlternateCostState();
    this.pendingAlternateExileHandIndex = selected[0];
    this.pendingAlternateExileHandIndices = selected;
    this.continuePlayCard(spellIndex);
  }

  toggleAlternateCostCreature(permanentId: string): void {
    if (!this.selectingAlternateCostCreatures) return;
    const totalNeeded = this.alternateCostSacrificeCount + this.alternateCostTapCount + this.alternateCostReturnCount;
    const current = this.alternateCostSelectedIds();
    if (current.includes(permanentId)) {
      this.alternateCostSelectedIds.set(current.filter(id => id !== permanentId));
    } else {
      if (current.length >= totalNeeded) return;
      this.alternateCostSelectedIds.set([...current, permanentId]);
    }
  }

  isAlternateCostSelected(permanentId: string): boolean {
    return this.alternateCostSelectedIds().includes(permanentId);
  }

  confirmAlternateCost(): void {
    if (!this.selectingAlternateCostCreatures) return;
    const totalNeeded = this.alternateCostSacrificeCount + this.alternateCostTapCount + this.alternateCostReturnCount;
    const selected = this.alternateCostSelectedIds();
    if (selected.length !== totalNeeded) return;
    this.websocketService.send({
      type: MessageType.PLAY_CARD,
      cardIndex: this.alternateCostCardIndex,
      alternateCostSacrificePermanentIds: selected
    });
    this.resetAlternateCostState();
  }

  cancelAlternateCost(): void {
    this.resetAlternateCostState();
  }

  private resetAlternateCostState(): void {
    this.choosingAlternateCost = false;
    this.selectingAlternateCostCreatures = false;
    this.selectingAlternateCostHandCard = false;
    this.alternateCostCardIndex = -1;
    this.alternateCostCardName = '';
    this.alternateCostSacrificeCount = 0;
    this.alternateCostLifePayment = 0;
    this.alternateCostTapCount = 0;
    this.alternateCostReturnCount = 0;
    this.alternateCostManaCost = '';
    this.alternateCostIsPlot = false;
    this.alternateCostExileHandCount = 0;
    this.alternateCostExileHandLabel = '';
    this.alternateCostRevealsHandCard = false;
    this.alternateCostDiscardsHandCard = false;
    this.alternateCostRequiresTarget = false;
    this.alternateCostSelectedIds.set([]);
    this.alternateCostSelectedHandIndices.set([]);
    this.pendingAlternateExileHandIndex = null;
    this.pendingAlternateHandCardIndices = [];
    this.pendingAlternateHandCardDiscards = false;
    this.pendingAlternateExileHandIndices = [];
  }

  toggleExileCounterCostPermanent(permanentId: string): void {
    if (!this.selectingExileCounterCost) return;
    const current = this.exileCounterCostSelectedIds();
    const permanent = this.myBattlefieldFn().find(p => p.id === permanentId);
    const available = permanent ? Object.values(permanent.counters ?? {}).reduce((sum, n) => sum + n, 0) : 0;
    const selectedForPermanent = current.filter(id => id === permanentId).length;
    if (current.length < this.exileCounterCostRequired && selectedForPermanent < available) {
      this.exileCounterCostSelectedIds.set([...current, permanentId]);
      return;
    }
    const existingIndex = current.indexOf(permanentId);
    if (existingIndex >= 0) {
      this.exileCounterCostSelectedIds.set([
        ...current.slice(0, existingIndex), ...current.slice(existingIndex + 1)
      ]);
    }
  }

  isExileCounterCostSelected(permanentId: string): boolean {
    return this.exileCounterCostSelectedIds().includes(permanentId);
  }

  selectedExileCounterCostCount(permanentId: string): number {
    return this.exileCounterCostSelectedIds().filter(id => id === permanentId).length;
  }

  confirmExileCounterCost(): void {
    if (!this.selectingExileCounterCost) return;
    const selected = this.exileCounterCostSelectedIds();
    if (selected.length !== this.exileCounterCostRequired || !this.pendingZoneCard) return;
    const card = this.pendingZoneCard;
    this.pendingExileCounterCostPermanentIds = [...selected];
    this.selectingExileCounterCost = false;
    this.exileCounterCostCardName = '';
    this.exileCounterCostRequired = 0;
    this.exileCounterCostSelectedIds.set([]);
    this.continueZonePlay(card);
  }

  cancelExileCounterCost(): void {
    this.selectingExileCounterCost = false;
    this.exileCounterCostCardName = '';
    this.exileCounterCostRequired = 0;
    this.exileCounterCostSelectedIds.set([]);
    this.pendingExileCounterCostPermanentIds = [];
    this.pendingZoneCard = null;
    this.pendingFromExileCardId = null;
  }

  // ========== Tap / ability activation ==========

  /** Sentinel ability index for the intrinsic ON_TAP mana option in the ability picker. */
  static readonly INTRINSIC_TAP_INDEX = -1;

  tapPermanent(index: number): void {
    const g = this.gameSignal();
    if (g && this.canTapPermanent(index)) {
      const perm = this.myBattlefieldFn()[index];
      if (!perm) return;

      const abilities = perm.card.activatedAbilities;
      if (abilities.length === 0) {
        // No activated abilities — just tap for mana (ON_TAP)
        this.sendTapPermanent(index);
        return;
      }

      // Intrinsic ON_TAP mana (e.g. a basic land's own mana) — must stay reachable even
      // when the permanent also has activated abilities (e.g. a Plains that gained
      // "{T}: Add {U}" can still produce white)
      const canIntrinsicTap = perm.card.hasTapAbility && !perm.tapped
        && !(perm.summoningSick && isPermanentCreature(perm));

      // While paying for a held-back cast/activation only mana production is actionable —
      // starting another non-mana activation would clobber the held message.
      const paying = this.payingForCast || this.payingForAbility;

      // Filter to usable abilities
      const usable = abilities.filter(a => this.abilityUsableNow(perm, a, paying));
      if (usable.length === 0) {
        // Has abilities but none usable — fall back to tap for mana if ON_TAP
        if (canIntrinsicTap) {
          this.sendTapPermanent(index);
        }
        return;
      }

      if (usable.length === 1 && !canIntrinsicTap) {
        // Single usable ability and no intrinsic tap — activate directly
        const abilityIndex = abilities.indexOf(usable[0]);
        this.activateAbilityAtIndex(index, abilityIndex, perm);
      } else {
        // Multiple options — show picker
        this.choosingAbility = true;
        this.abilityChoicePermanentIndex = index;
        this.abilityChoices = abilities.map((a, i) => ({ ability: a, index: i, usable: this.abilityUsableNow(perm, a, paying) }));
        if (canIntrinsicTap) {
          this.abilityChoices.unshift({
            ability: this.intrinsicTapAbilityView(perm),
            index: TargetingChoiceService.INTRINSIC_TAP_INDEX,
            usable: true
          });
        }
      }
    }
  }

  private intrinsicTapAbilityView(perm: Permanent): ActivatedAbilityView {
    // Show the land's own printed mana line when derivable (e.g. Plains: "({T}: Add {W}.)")
    const printed = perm.card.cardText?.match(/\{T\}: Add [^)\n]+/);
    return {
      description: printed ? printed[0] : '{T}: Tap for mana.',
      requiresTap: true,
      needsTarget: false,
      needsSpellTarget: false,
      manaCost: null,
      loyaltyCost: null,
      minTargets: 0,
      maxTargets: 0,
      isManaAbility: true,
      variableLoyaltyCost: false,
      variableCounterCostType: null,
      requiresXValue: false,
      xValueFromControlledCreatureCounters: false
    };
  }

  /** Whether the ability is a legal click right now: any usable ability normally, but only
      strictly affordable mana abilities while a cast/activation payment is in progress. */
  private abilityUsableNow(perm: Permanent, ability: ActivatedAbilityView, paying: boolean): boolean {
    if (paying) {
      return ability.isManaAbility && this.canUseAbility(perm, ability, false);
    }
    return this.canUseAbility(perm, ability);
  }

  canUseAbility(perm: Permanent, ability: ActivatedAbilityView, allowPotentialMana = true): boolean {
    if (ability.loyaltyCost != null || ability.variableLoyaltyCost) {
      const g = this.gameSignal();
      if (!g) return false;
      const myId = this.websocketService.currentUser?.userId;
      // Sorcery-speed: must be active player
      if (g.activePlayerId !== myId) return false;
      // Main phase only
      if (g.currentStep !== 'PRECOMBAT_MAIN' && g.currentStep !== 'POSTCOMBAT_MAIN') return false;
      // Stack must be empty
      if (g.stack.length > 0) return false;
      // Variable loyalty cost: just need the planeswalker to exist (X can be 0)
      if (ability.variableLoyaltyCost) return true;
      // Negative loyalty cost: check sufficient loyalty
      if (ability.loyaltyCost! < 0 && (perm.counters?.['LOYALTY'] ?? 0) < Math.abs(ability.loyaltyCost!)) return false;
      return true;
    }
    if (ability.requiresTap) {
      if (perm.tapped) return false;
      if (perm.summoningSick && isPermanentCreature(perm)) return false;
    }
    if (ability.requiresXValue && !ability.xValueFromCardsInHandColor
      && this.availableXValue(perm, ability) < 1) return false;
    if (ability.manaCost && !this.canPayManaCost(ability.manaCost)
        && !(allowPotentialMana && this.isPotentiallyPayableAbility(perm, ability))) return false;
    return true;
  }

  /** +1/+1 counters the ability's X may draw on: those on the source, or — for a
      "from among creatures you control" cost — those on every creature the player controls. */
  private availableXCounters(perm: Permanent, ability: ActivatedAbilityView): number {
    if (!ability.xValueFromControlledCreatureCounters) return perm.counters?.['PLUS_ONE_PLUS_ONE'] ?? 0;
    return this.myBattlefieldFn()
      .filter(p => isPermanentCreature(p))
      .reduce((sum, p) => sum + (p.counters?.['PLUS_ONE_PLUS_ONE'] ?? 0), 0);
  }

  private availableXValue(perm: Permanent, ability: ActivatedAbilityView): number {
    if (ability.xValueFromCardsInHandColor) {
      return this.gameSignal()?.hand?.filter(card =>
        card.colors?.includes(ability.xValueFromCardsInHandColor!)).length ?? 0;
    }
    return this.availableXCounters(perm, ability);
  }

  /** MTGO-style: an ability whose cost exceeds the floating pool is still activatable when
      the server marked it payable after tapping every untapped mana source — activating it
      enters payment mode. The server list is the activated-ability counterpart of
      potentialPlayableCardIndices: color-aware and dual-land-correct. */
  private isPotentiallyPayableAbility(perm: Permanent, ability: ActivatedAbilityView): boolean {
    const indices = this.potentialPayableAbilityIndicesFn()[perm.id];
    if (!indices) return false;
    const abilityIndex = perm.card.activatedAbilities.indexOf(ability);
    return abilityIndex >= 0 && indices.includes(abilityIndex);
  }

  private canPayManaCost(manaCost: string, extraGeneric = 0): boolean {
    const g = this.gameSignal();
    if (!g) return false;
    const pool = g.manaPool;
    const symbols = manaCost.match(/\{([^}]+)\}/g) || [];
    const coloredSymbols = ['W', 'U', 'B', 'R', 'G', 'C'];
    const coloredNeeded: Record<string, number> = {};
    let genericNeeded = extraGeneric;
    for (const sym of symbols) {
      const inner = sym.slice(1, -1);
      if (inner === 'X' || inner === 'T') continue;
      // Phyrexian mana (e.g. R/P) is always payable with 2 life — skip it
      if (inner.endsWith('/P')) continue;
      if (coloredSymbols.includes(inner)) {
        coloredNeeded[inner] = (coloredNeeded[inner] ?? 0) + 1;
      } else {
        const num = parseInt(inner);
        if (!isNaN(num)) genericNeeded += num;
      }
    }
    // Check each colored requirement
    let totalUsed = 0;
    for (const [color, needed] of Object.entries(coloredNeeded)) {
      if ((pool[color] ?? 0) < needed) return false;
      totalUsed += needed;
    }
    // Check generic requirement against remaining mana
    const totalAvailable = Object.values(pool).reduce((sum, v) => sum + v, 0);
    if (totalAvailable - totalUsed < genericNeeded) return false;
    return true;
  }

  activateAbilityAtIndex(permanentIndex: number, abilityIndex: number, perm: Permanent): void {
    const ability = perm.card.activatedAbilities[abilityIndex];

    if ((ability.modalChoicesRequired ?? 0) > 0 && (ability.modalOptions?.length ?? 0) > 0) {
      this.choosingMode = true;
      this.modeForAbility = true;
      this.modeAbilityPermanentIndex = permanentIndex;
      this.modeAbilityIndex = abilityIndex;
      this.modeCardIndex = -1;
      this.modeCardName = perm.card.name;
      this.modeOptions = ability.modalOptions ?? [];
      this.modeChoicesRequired = ability.modalChoicesRequired ?? 1;
      this.modeChoicesMax = ability.modalChoicesMax ?? this.modeChoicesRequired;
      this.modeOptional = false;
      this.modeSelectedIndices = [];
      return;
    }

    if (ability.requiresXValue) {
      this.choosingXValue = true;
      this.xValueCardIndex = permanentIndex;
      this.xValueCardName = perm.card.name;
      this.xValueInput = ability.xValueFromCardsInHandColor ? 0 : 1;
      this.xValueMaximum = this.availableXValue(perm, ability);
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Check for variable loyalty cost (-X)
    if (ability.variableLoyaltyCost) {
      this.choosingXValue = true;
      this.xValueCardIndex = permanentIndex;
      this.xValueCardName = perm.card.name;
      this.xValueInput = 0;
      this.xValueMaximum = perm.counters?.['LOYALTY'] ?? 0;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

    // Remove-X-counters cost (Night Dealings): X is capped by the counters on the permanent, and
    // the mana cost is paid separately, so this is a counter prompt rather than a mana one.
    if (ability.variableCounterCostType) {
      this.choosingXValue = true;
      this.xValueCardIndex = permanentIndex;
      this.xValueCardName = perm.card.name;
      this.xValueInput = 0;
      this.xValueMaximum = perm.counters?.[ability.variableCounterCostType] ?? 0;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      return;
    }

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
      // X can be paid MTGO-style by tapping more lands after announcing, so the cap is
      // the potential mana (pool + untapped sources), not just what's floating now.
      this.xValueMaximum = Math.max(this.totalManaFn(), this.potentialTotalManaFn()) - base;
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

    // Check for targeting — request valid targets from backend
    if (ability.needsTarget) {
      this.targetingCardIndex = permanentIndex;
      this.targetingCardName = perm.card.name;
      this.targetingForAbility = true;
      this.targetingAbilityIndex = abilityIndex;
      this.pendingAbilityXValue = null;
      this.multiTargetCardIndex = permanentIndex;
      this.multiTargetCardName = perm.card.name;
      this.sendValidTargetsRequest(null, permanentIndex, abilityIndex);
      return;
    }

    // No target or X needed — send immediately (or enter payment mode if unaffordable)
    this.sendActivateAbilityMessage({
      type: MessageType.ACTIVATE_ABILITY,
      permanentIndex,
      abilityIndex
    });
  }

  chooseAbility(choice: { ability: ActivatedAbilityView; index: number; usable: boolean }): void {
    if (!choice.usable) return;
    const perm = this.myBattlefieldFn()[this.abilityChoicePermanentIndex];
    if (!perm) return;
    if (choice.index === TargetingChoiceService.INTRINSIC_TAP_INDEX) {
      this.sendTapPermanent(this.abilityChoicePermanentIndex);
    } else {
      this.activateAbilityAtIndex(this.abilityChoicePermanentIndex, choice.index, perm);
    }
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
    // Mid-targeting: the player must lock in a target before they can tap mana.
    if (this.selectingCastTarget) return false;
    // The held activation's own {T} cost: tapping the source for mana would break the
    // pending activation, so it stays locked while its payment is in progress.
    if (this.payingForAbility && this.pendingActivationRequiresTap
        && perm.id === this.pendingActivationPermanentId) return false;
    const abilities = perm.card.activatedAbilities;
    // Check if any activated ability can be used right now
    const paying = this.payingForCast || this.payingForAbility;
    if (abilities.some(a => this.abilityUsableNow(perm, a, paying))) return true;
    // Can tap for mana (ON_TAP mana effects)
    if (perm.tapped) return false;
    if (!perm.card.hasTapAbility) return false;
    if (perm.summoningSick && isPermanentCreature(perm)) return false;
    return true;
  }
}
