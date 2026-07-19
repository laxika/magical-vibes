import { Injectable, Signal, signal } from '@angular/core';
import {
  WebsocketService, Game, MessageType, Card, Permanent, StackEntry, ActivatedAbilityView,
  ValidTargetsResponse, ModalOptionView
} from './websocket.service';
import { isPermanentCreature } from '../components/game/battlefield.utils';

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
    // Kicker
    this.choosingKicker = false;
    this.kickerCardIndex = -1;
    this.kickerCardName = '';
    this.kickerCost = '';
    this.pendingKicked = false;
    // Modal mode picker
    this.choosingMode = false;
    this.modeCardIndex = -1;
    this.modeCardName = '';
    this.modeOptions = [];
    this.modeChoicesRequired = 1;
    this.modeOptional = false;
    this.modeSelectedIndices = [];
    this.spellTargetCount = 1;
    this.spellTargetSelectedIds = [];
    // Flashback
    this.pendingFlashback = false;
    // Exile / library-top casting
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
    // Alternate casting cost
    this.choosingAlternateCost = false;
    this.selectingAlternateCostCreatures = false;
    this.alternateCostCardIndex = -1;
    this.alternateCostCardName = '';
    this.alternateCostSacrificeCount = 0;
    this.alternateCostLifePayment = 0;
    this.alternateCostTapCount = 0;
    this.alternateCostManaCost = '';
    this.alternateCostSelectedIds.set([]);
    // Graveyard targeting
    this.targetingGraveyard = false;
    this.graveyardTargetCards = [];
    this.graveyardTargetCardIds = [];
    this.graveyardTargetPrompt = '';
    // MTGO-style cast payment
    this.clearCastPayment();
    this.clearAbilityPayment();
  }

  private get hasPriority(): boolean {
    const g = this.gameSignal();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
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
    return this.selectingTarget || this.targetingSpell || this.multiTargeting || this.targetingGraveyard;
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
  modeOptional = false;
  modeSelectedIndices: number[] = [];
  // Multi-spell-target modal modes (e.g. "copy target instant and target creature spell")
  spellTargetCount = 1;
  spellTargetSelectedIds: string[] = [];

  // --- Targeting state (for instants and activated abilities) ---
  selectingTarget = false;
  targetingCardIndex = -1;
  targetingCardName = '';
  targetingForAbility = false;
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

  // --- Kicker state ---
  choosingKicker = false;
  kickerCardIndex = -1;
  kickerCardName = '';
  kickerCost = '';
  private pendingKicked = false;

  // --- Flashback state ---
  private pendingFlashback = false;

  // --- Exile / library-top casting state ---
  private pendingFromExileCardId: string | null = null;
  private pendingFromLibraryTop = false;
  private pendingZoneCard: Card | null = null;

  // --- Alternate casting cost state ---
  choosingAlternateCost = false;
  selectingAlternateCostCreatures = false;
  alternateCostCardIndex = -1;
  alternateCostCardName = '';
  alternateCostSacrificeCount = 0;
  alternateCostLifePayment = 0;
  alternateCostTapCount = 0;
  alternateCostManaCost = '';
  alternateCostSelectedIds = signal<string[]>([]);

  // --- Graveyard targeting state ---
  targetingGraveyard = false;
  graveyardTargetCards: Card[] = [];
  graveyardTargetCardIds: string[] = [];
  graveyardTargetPrompt = '';

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

    // No valid targets — auto-cancel to prevent stuck UI
    if (msg.validPermanentIds.length === 0 && msg.validPlayerIds.length === 0
        && !hasGraveyardTargets && msg.minTargets > 0) {
      this.resetTargetingState();
      this.cancelMultiTargeting();
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

  private sendValidTargetsRequest(cardIndex: number | null, permanentIndex: number | null, abilityIndex: number | null, alreadySelectedIds: string[] = [], xValue: number | null = null): void {
    this.pendingTargetRequest = true;
    const msg: any = {
      type: MessageType.VALID_TARGETS_REQUEST,
      cardIndex,
      permanentIndex,
      abilityIndex,
      alreadySelectedIds
    };
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
        this.alternateCostManaCost = card.alternateCostManaCost ?? '';
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

      this.continuePlayCard(index);
    }
  }

  private continuePlayCard(index: number): void {
    const g = this.gameSignal();
    if (!g) return;
    const card = g.hand[index];
    if (!card) return;

    // Modal ("choose one/two") spell or ETB — pick mode(s) before anything else
    if (card.modalChoicesRequired > 0 && card.modalOptions && card.modalOptions.length > 0) {
      this.choosingMode = true;
      this.modeCardIndex = index;
      this.modeCardName = card.name;
      this.modeOptions = card.modalOptions;
      this.modeChoicesRequired = card.modalChoicesRequired;
      this.modeOptional = card.modalOptional;
      this.modeSelectedIndices = [];
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
    this.choosingKicker = false;
    this.kickerCardIndex = -1;
    this.kickerCardName = '';
    this.kickerCost = '';
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
  }

  // ========== Modal mode picker ==========

  toggleMode(optionIndex: number): void {
    if (!this.choosingMode) return;
    if (this.modeChoicesRequired === 1) {
      this.modeSelectedIndices = [optionIndex];
      return;
    }
    if (this.modeSelectedIndices.includes(optionIndex)) {
      this.modeSelectedIndices = this.modeSelectedIndices.filter(i => i !== optionIndex);
    } else if (this.modeSelectedIndices.length < this.modeChoicesRequired) {
      this.modeSelectedIndices = [...this.modeSelectedIndices, optionIndex];
    }
  }

  isModeSelected(optionIndex: number): boolean {
    return this.modeSelectedIndices.includes(optionIndex);
  }

  /**
   * Encodes the mode selection the same way the engine's ChooseOneEffect.encodeModeSelection
   * does: choose-one spells use the 0-based mode index, choose-two (or higher) spells use a
   * negative bitmask.
   */
  private encodeModeSelection(indices: number[]): number {
    if (this.modeChoicesRequired === 1) {
      return indices[0];
    }
    let mask = 0;
    for (const i of indices) {
      mask |= (1 << i);
    }
    return -mask;
  }

  confirmModes(): void {
    if (!this.choosingMode || this.modeSelectedIndices.length !== this.modeChoicesRequired) return;
    const g = this.gameSignal();
    if (!g) return;

    const cardIndex = this.modeCardIndex;
    const cardName = this.modeCardName;
    const zoneCard = this.pendingZoneCard;
    const card = zoneCard ?? g.hand[cardIndex];
    const chosen = this.modeSelectedIndices.map(i => this.modeOptions[i]);
    const encoded = this.encodeModeSelection(this.modeSelectedIndices);
    this.resetModeState();

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
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
  }

  private resetModeState(): void {
    this.choosingMode = false;
    this.modeCardIndex = -1;
    this.modeCardName = '';
    this.modeOptions = [];
    this.modeChoicesRequired = 1;
    this.modeOptional = false;
    this.modeSelectedIndices = [];
  }

  startFlashbackTargeting(graveyardIndex: number, card: Card): void {
    this.pendingFlashback = true;
    this.targetingCardIndex = graveyardIndex;
    this.targetingCardName = card.name;
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
    this.sendValidTargetsRequest(null, null, null);
    // Use the card's targeting info — request valid targets from graveyard card
    // The backend ValidTargets handler uses cardIndex from hand. For flashback,
    // we manually set valid targets based on battlefield artifacts.
    // Actually, we can piggyback on the existing targeting: the user clicks a permanent.
    // For simplicity, enter selectingTarget mode and let the backend validate on cast.
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
    // Modal ("choose one/two") spell — pick mode(s) before anything else
    if (card.modalChoicesRequired > 0 && card.modalOptions && card.modalOptions.length > 0) {
      this.pendingZoneCard = card;
      this.choosingMode = true;
      this.modeCardIndex = 0;
      this.modeCardName = card.name;
      this.modeOptions = card.modalOptions;
      this.modeChoicesRequired = card.modalChoicesRequired;
      this.modeOptional = card.modalOptional;
      this.modeSelectedIndices = [];
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

  private sendPlayCardMessage(cardIndex: number, targetId: string | null, extra?: Record<string, any>): void {
    const msg: any = {
      type: MessageType.PLAY_CARD,
      cardIndex,
      targetId
    };
    if (this.pendingPhyrexianLifeCount != null) {
      msg.phyrexianLifeCount = this.pendingPhyrexianLifeCount;
    }
    if (this.pendingFlashback) {
      msg.flashback = true;
      this.pendingFlashback = false;
    }
    if (this.pendingFromExileCardId != null) {
      msg.fromExileCardId = this.pendingFromExileCardId;
      this.pendingFromExileCardId = null;
    }
    if (this.pendingFromLibraryTop) {
      msg.fromLibraryTop = true;
      this.pendingFromLibraryTop = false;
    }
    this.pendingZoneCard = null;
    if (this.pendingKicked) {
      msg.kicked = true;
      this.pendingKicked = false;
    }
    if (extra) {
      Object.assign(msg, extra);
    }
    this.pendingPhyrexianLifeCount = null;

    // MTGO-style casting: a plain hand cast whose cost the pool doesn't cover yet is
    // held back while the player taps mana sources; it is sent automatically once the
    // pool covers it (see onGameStateUpdate). Zone plays (flashback/exile/library-top)
    // keep the immediate path — the server marked them strictly affordable.
    const isZonePlay = msg.flashback || msg.fromExileCardId != null || msg.fromLibraryTop;
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
    if (!g || cardChanged || !this.hasPriority) {
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
    if (this.beginAbilityPaymentIfUnaffordable(msg)) return;
    this.websocketService.send(msg);
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
    if (!g || index < 0 || !this.hasPriority) {
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
        this.sendValidTargetsRequest(null, this.xValueCardIndex, this.targetingAbilityIndex);
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
  }

  selectTarget(permanentId: string): void {
    if (!this.selectingTarget) return;
    if (!this.validTargetIds().has(permanentId)) return;
    if (this.targetingForAbility) {
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
      this.sendActivateAbilityMessage({
        type: MessageType.ACTIVATE_ABILITY,
        permanentIndex: this.targetingCardIndex,
        abilityIndex: this.targetingAbilityIndex,
        targetId: playerId
      });
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

  cancelGraveyardTargeting(): void {
    this.targetingGraveyard = false;
    this.graveyardTargetCards = [];
    this.graveyardTargetCardIds = [];
    this.graveyardTargetPrompt = '';
    this.resetTargetingState();
  }

  private resetTargetingState(): void {
    this.selectingTarget = false;
    this.targetingCardIndex = -1;
    this.targetingCardName = '';
    this.targetingForAbility = false;
    this.targetingAbilityIndex = -1;
    this.validTargetIds.set(new Set());
    this.validTargetPlayerIds.set(new Set());
    this.targetingPrompt = '';
    this.pendingAbilityXValue = null;
    this.pendingConvokeCard = null;
    // A completed cast consumes these in sendPlayCardMessage before we get here;
    // clearing them covers the cancel paths so the flags can't leak into a later cast.
    this.pendingFlashback = false;
    this.pendingFromExileCardId = null;
    this.pendingFromLibraryTop = false;
    this.pendingZoneCard = null;
    // Note: don't reset pendingPhyrexianLifeCount here — it carries through to the final send
  }

  cancelTargeting(): void {
    this.resetTargetingState();
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
      if (this.targetingForAbility) {
        this.sendValidTargetsRequest(null, this.multiTargetCardIndex, this.targetingAbilityIndex, newSelected);
      } else {
        this.sendValidTargetsRequest(this.multiTargetCardIndex, null, null, newSelected);
      }
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
      if (this.targetingForAbility) {
        this.sendValidTargetsRequest(null, this.multiTargetCardIndex, this.targetingAbilityIndex, newSelected);
      } else {
        this.sendValidTargetsRequest(this.multiTargetCardIndex, null, null, newSelected);
      }
    }
  }

  removeMultiTarget(permanentId: string): void {
    if (!this.multiTargeting) return;
    const newSelected = this.multiTargetSelectedIds().filter(id => id !== permanentId);
    this.multiTargetSelectedIds.set(newSelected);
    // Request refreshed valid targets from backend with updated already-selected list
    if (this.targetingForAbility) {
      this.sendValidTargetsRequest(null, this.multiTargetCardIndex, this.targetingAbilityIndex, newSelected);
    } else {
      this.sendValidTargetsRequest(this.multiTargetCardIndex, null, null, newSelected);
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
    this.addPendingPhyrexianToMsg(msg);
    this.addPendingKickedToMsg(msg);
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

  choosePayMana(): void {
    const savedIndex = this.alternateCostCardIndex;
    this.resetAlternateCostState();
    this.continuePlayCard(savedIndex);
  }

  choosePayAlternateCost(): void {
    this.choosingAlternateCost = false;
    this.selectingAlternateCostCreatures = true;
    this.alternateCostSelectedIds.set([]);
  }

  toggleAlternateCostCreature(permanentId: string): void {
    if (!this.selectingAlternateCostCreatures) return;
    const totalNeeded = this.alternateCostSacrificeCount + this.alternateCostTapCount;
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
    const totalNeeded = this.alternateCostSacrificeCount + this.alternateCostTapCount;
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
    this.alternateCostCardIndex = -1;
    this.alternateCostCardName = '';
    this.alternateCostSacrificeCount = 0;
    this.alternateCostLifePayment = 0;
    this.alternateCostTapCount = 0;
    this.alternateCostManaCost = '';
    this.alternateCostSelectedIds.set([]);
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
        this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
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
          this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
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
      variableLoyaltyCost: false
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
    if (ability.manaCost && !this.canPayManaCost(ability.manaCost)
        && !(allowPotentialMana && this.isPotentiallyPayableAbility(perm, ability))) return false;
    return true;
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
      this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: this.abilityChoicePermanentIndex });
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
