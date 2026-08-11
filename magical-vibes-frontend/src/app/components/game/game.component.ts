import { Component, OnInit, OnDestroy, ViewChild, ElementRef, HostListener, NgZone, ChangeDetectorRef, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, WebSocketMessage, Game, GameNotification, GameStateNotification, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, Permanent, MulliganResolvedNotification, SelectCardsToBottomNotification, AttackTarget, AvailableAttackersNotification, AvailableBlockersNotification, GameOverNotification, StackEntry, RevealHandNotification, InteractionPromptNotification, RevealLibraryTopNotification, CombatDamageAssignmentNotification, ValidTargetsResponse, GameLogEntry } from '../../services/websocket.service';
import { GameChoiceService } from '../../services/game-choice.service';
import { CardDisplayComponent } from './card-display/card-display.component';
import { MulliganModalComponent } from './mulligan-modal/mulligan-modal.component';
import { SidePanelComponent } from './side-panel/side-panel.component';
import { ModifierTooltipComponent } from './modifier-tooltip/modifier-tooltip.component';
import { IndexedPermanent, AttachedAura, LandStack, canFormAttackingBand, splitBattlefield, stackBasicLands, getAttachedAuras, isLandStack, isPermanentCreature, isPermanentArtifact, isPermanentLand } from './battlefield.utils';
import { BattlefieldFitModel, BattlefieldSide } from './battlefield-zoom';
import { Subscription } from 'rxjs';
import { manaSymbolHtml } from '../../utils/mana-symbols';
import { PermanentClickResolverService } from '../../services/permanent-click-resolver.service';

/** Template context for the single #permanentStack definition in game.component.html. */
export interface PermanentStackContext {
  $implicit: IndexedPermanent;
  mine: boolean;
  creature: boolean;
}

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, FormsModule, CardDisplayComponent, MulliganModalComponent, SidePanelComponent, ModifierTooltipComponent],
  templateUrl: './game.component.html',
  styleUrls: ['./shared-game-styles.css', './game.component.css', './game-phone.css']
})
export class GameComponent implements OnInit, OnDestroy {
  game = signal<Game | null>(null);
  hoveredCard = signal<Card | null>(null);
  hoveredPermanent = signal<Permanent | null>(null);
  // Anchor for the board-card modifier tooltip; set only after 3s of continuous hover
  // over a battlefield permanent (the timer below defers it).
  modifierTooltipAnchor = signal<{ x: number; y: number; below: boolean } | null>(null);
  private modifierTooltipTimer: ReturnType<typeof setTimeout> | null = null;
  stackTargetId = signal<string | null>(null);
  private subscriptions: Subscription[] = [];

  @ViewChild(MulliganModalComponent) mulliganModal?: MulliganModalComponent;
  @ViewChild(SidePanelComponent) sidePanel?: SidePanelComponent;

  private battlefieldAreaObserver: ResizeObserver | null = null;
  readonly battlefieldAreaSize = signal<{ width: number; height: number }>({ width: 0, height: 0 });

  @ViewChild('battlefieldArea')
  set battlefieldArea(ref: ElementRef<HTMLElement> | undefined) {
    this.battlefieldAreaObserver?.disconnect();
    this.battlefieldAreaEl = ref?.nativeElement ?? null;
    if (!ref) return;
    this.battlefieldAreaObserver ??= new ResizeObserver(entries => {
      const rect = entries[entries.length - 1].contentRect;
      this.ngZone.run(() => {
        this.battlefieldAreaSize.set({ width: rect.width, height: rect.height });
        this.scheduleCombatShiftUpdate();
      });
    });
    this.battlefieldAreaObserver.observe(ref.nativeElement);
  }

  private ngZone = inject(NgZone);
  private cdr = inject(ChangeDetectorRef);
  readonly choice = inject(GameChoiceService);
  private clickResolver = inject(PermanentClickResolverService);
  private sanitizer = inject(DomSanitizer);

  // Bound function references for child component inputs
  readonly boundIsGraveyardLandPlayable = (index: number) => this.isGraveyardLandPlayable(index);
  readonly boundIsGraveyardAbilityActivatable = (index: number) => this.isGraveyardAbilityActivatable(index);
  readonly boundIsFlashbackPlayable = (index: number) => this.isFlashbackPlayable(index);
  readonly boundGetPlayerName = (playerId: string) => this.getPlayerName(playerId);
  readonly boundGetStackEntryTargetName = (entry: StackEntry) => this.getStackEntryTargetName(entry);

  constructor(
    private router: Router,
    private websocketService: WebsocketService
  ) {}

  ngOnInit() {
    if (!this.websocketService.isConnected() || !this.websocketService.currentGame) {
      this.router.navigate(['/']);
      return;
    }

    this.game.set(this.websocketService.currentGame);

    // Reset local component state from any previous game
    this.gameOverActive.set(false);
    this.gameOverWinner.set(null);
    this.gameOverWinnerId.set(null);
    this.declaringAttackers.set(false);
    this.declaringBlockers.set(false);
    this.choosingBlocksForOpponent.set(false);
    this.attackTaxPerCreature.set(0);
    this.mustAttackWithAtLeastOne.set(false);
    this.availableAttackerIndices.set(new Set());
    this.mustAttackIndices.set(new Set());
    this.availableBlockerIndices.set(new Set());
    this.selectedAttackerIndices.set(new Set());
    this.bandAssignments.set(new Map());
    this.opponentAttackerIndices.set([]);
    this.blockerAssignments.set(new Map());
    this.legalBlockPairs.set(new Map());
    this.selectedBlockerIndex.set(null);
    this.playableCardIndices.set(new Set());
    this.potentialPlayableCardIndices.set(new Set());
    this.potentialManaTotal.set(0);
    this.potentialPayableAbilityIndices.set({});
    this.playableGraveyardLandIndices.set(new Set());
    this.playableFlashbackIndices.set(new Set());
    this.playableExileCards.set([]);
    this.playableLibraryTopCards.set([]);
    this.searchTaxCost.set(0);
    this.hoveredCard.set(null);
    this.hoveredPermanent.set(null);
    this.clearModifierTooltip();
    this.stackTargetId.set(null);
    this.combatShiftX.set(new Map());
    this.showShortcutsPopup.set(false);
    this.connectionLost.set(false);

    this.choice.init(
      this.game,
      () => this.myBattlefield,
      () => this.opponentBattlefield,
      () => this.totalMana,
      (index: number) => this.playableCardIndices().has(index),
      () => this.potentialManaTotal(),
      () => this.potentialPayableAbilityIndices()
    );

    const initialStops = this.websocketService.currentGame?.autoStopSteps;
    if (initialStops) {
      this.autoStopSteps.set(new Set(initialStops));
    }

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        this.processGameMessage(message);
      })
    );

    // Replay any game input message that arrived before this component subscribed (e.g. during rejoin)
    const pending = this.websocketService.pendingGameInputMessage;
    if (pending) {
      this.websocketService.pendingGameInputMessage = null;
      this.processGameMessage(pending);
    }

    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        /* Losing the socket used to navigate straight to the login screen, which read
           as the client crashing. Raise a blocking notice over the last known board
           instead: the player can see where the game was, and is told the game itself
           survives. Reconnecting silently is not an option — the socket carries the
           session and the service drops the credentials on cleanup(), so getting back
           in genuinely means logging in again. */
        this.connectionLost.set(true);
      })
    );
  }

  /** The socket dropped mid-game. The board behind the notice is the last state we saw. */
  connectionLost = signal(false);

  returnToLogin(): void {
    this.router.navigate(['/']);
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
    if (this.modifierTooltipTimer != null) {
      clearTimeout(this.modifierTooltipTimer);
    }
    this.battlefieldAreaObserver?.disconnect();
    this.onRevealHandDragEnd();
    if (this.combatShiftFrame != null) {
      cancelAnimationFrame(this.combatShiftFrame);
    }
    this.websocketService.pendingGameInputMessage = null;
  }

  private processGameMessage(message: WebSocketMessage): void {
    console.log(message);

    // The app is zoneless, and WebSocket callbacks don't schedule change detection.
    // Most messages update signals (which do), but some (e.g. VALID_TARGETS_RESPONSE)
    // only mutate plain service state that templates read — mark the view dirty so
    // every message renders.
    this.cdr.markForCheck();

    if (message.type === MessageType.OPPONENT_JOINED) {
      const notification = message as GameNotification;
      if (notification.game) {
        this.game.set(notification.game);
        this.websocketService.currentGame = notification.game;
      }
    }

    if (message.type === MessageType.GAME_STATE) {
      this.applyGameState(message as GameStateNotification);
    }

    if (message.type === MessageType.MULLIGAN_RESOLVED) {
      this.mulliganModal?.handleMulliganResolved(
        message as MulliganResolvedNotification,
        this.websocketService.currentUser?.username ?? ''
      );
    }

    if (message.type === MessageType.SELECT_CARDS_TO_BOTTOM) {
      this.mulliganModal?.handleSelectCardsToBottom(message as SelectCardsToBottomNotification);
    }

    if (message.type === MessageType.AVAILABLE_ATTACKERS) {
      this.handleAvailableAttackers(message as AvailableAttackersNotification);
    }

    if (message.type === MessageType.AVAILABLE_BLOCKERS) {
      this.handleAvailableBlockers(message as AvailableBlockersNotification);
    }

    if (message.type === MessageType.GAME_OVER) {
      this.handleGameOver(message as GameOverNotification);
    }

    if (message.type === MessageType.INTERACTION_PROMPT) {
      const prompt = message as InteractionPromptNotification;
      if (prompt.shape === 'CARD_INDEX_PICK' && prompt.cards) {
        this.revealHandPos.set(null);
      }
      this.choice.handleInteractionPrompt(prompt);
    }

    if (message.type === MessageType.REVEAL_HAND) {
      this.revealHandPos.set(null);
      this.choice.handleRevealHand(message as RevealHandNotification);
    }

    if (message.type === MessageType.REVEAL_LIBRARY_TOP) {
      this.choice.handleRevealLibraryTop(message as RevealLibraryTopNotification);
    }

    if (message.type === MessageType.COMBAT_DAMAGE_ASSIGNMENT) {
      this.choice.handleCombatDamageAssignment(message as CombatDamageAssignmentNotification);
    }

    if (message.type === MessageType.VALID_TARGETS_RESPONSE) {
      this.choice.handleValidTargetsResponse(message as ValidTargetsResponse);
    }

  }

  // ========== Player info getters ==========

  get player1Name(): string {
    const g = this.game();
    return g && g.playerNames.length > 0 ? g.playerNames[0] : '';
  }

  get player2Name(): string {
    const g = this.game();
    return g && g.playerNames.length > 1 ? g.playerNames[1] : '';
  }

  get gameLog(): GameLogEntry[] {
    return this.game()?.gameLog ?? [];
  }

  get isWaitingForOpponent(): boolean {
    const g = this.game();
    return g !== null && g.playerNames.length < 2;
  }

  get isMyTurn(): boolean {
    const g = this.game();
    return g !== null && g.activePlayerId === this.websocketService.currentUser?.userId;
  }

  isActivePlayer(playerIndex: number): boolean {
    const g = this.game();
    return g !== null && g.playerIds?.[playerIndex] === g.activePlayerId;
  }

  holdsPriority(playerIndex: number): boolean {
    const g = this.game();
    return g !== null && g.playerIds?.[playerIndex] === g.priorityPlayerId;
  }

  get hasPriority(): boolean {
    const g = this.game();
    return g !== null && g.priorityPlayerId === this.websocketService.currentUser?.userId;
  }

  get myPlayerIndex(): number {
    const g = this.game();
    if (!g) return 0;
    return g.playerIds.indexOf(this.websocketService.currentUser?.userId ?? '');
  }

  get opponentPlayerIndex(): number {
    return this.myPlayerIndex === 0 ? 1 : 0;
  }

  get myBattlefield(): Permanent[] {
    return this.game()?.battlefields?.[this.myPlayerIndex] ?? [];
  }

  get opponentBattlefield(): Permanent[] {
    return this.game()?.battlefields?.[this.opponentPlayerIndex] ?? [];
  }

  /* The fit model lives in battlefield-zoom.ts so the tutorial's fake board sizes its
     cards exactly like a real one. Auras and exiled-with cards only exist here, so the
     tucked-card hook is the only part the game supplies. */
  private readonly fitModel = new BattlefieldFitModel(perm =>
    this.getAttachedAuras(perm.id).length + this.exiledWithCount(perm));

  private get opponentSide(): BattlefieldSide {
    return {
      creatures: this.opponentCreatures(),
      lands: this.opponentLandStacks,
      isEmpty: this.opponentBattlefield.length === 0,
      revealedRows: (this.opponentHand.length > 0 ? 1 : 0) + (this.opponentRevealedTopCard.length > 0 ? 1 : 0),
    };
  }

  private get mySide(): BattlefieldSide {
    return {
      creatures: this.myCreatures(),
      lands: this.myLandStacks,
      isEmpty: this.myBattlefield.length === 0,
      revealedRows: (this.myRevealedTopCard.length > 0 ? 1 : 0) + (this.playableExileCards().length > 0 ? 1 : 0),
    };
  }

  /* Each player's row is zoomed to fit only its own content into its own half, so
     the opponent's board never resizes our cards (and vice versa) — unchanged by
     combat, since combat creatures stay in their rows at the same size. */
  get myBattlefieldZoom(): number {
    return this.fitModel.sideZoom(this.mySide, this.battlefieldAreaSize());
  }

  get opponentBattlefieldZoom(): number {
    return this.fitModel.sideZoom(this.opponentSide, this.battlefieldAreaSize());
  }

  /** Board-wide zoom, kept as the battlefield area's fallback density. */
  get battlefieldZoom(): number {
    return this.fitModel.boardZoom([this.mySide, this.opponentSide], this.battlefieldAreaSize());
  }

  get handZoom(): number {
    return BattlefieldFitModel.handZoom(this.hand.length);
  }

  get myGraveyard(): Card[] {
    return this.game()?.graveyards?.[this.myPlayerIndex] ?? [];
  }

  get opponentGraveyard(): Card[] {
    return this.game()?.graveyards?.[this.opponentPlayerIndex] ?? [];
  }

  get myRevealedTopCard(): Card[] {
    return this.game()?.revealedLibraryTopCards?.[this.myPlayerIndex] ?? [];
  }

  get opponentRevealedTopCard(): Card[] {
    return this.game()?.revealedLibraryTopCards?.[this.opponentPlayerIndex] ?? [];
  }

  get isMindControlling(): boolean {
    const g = this.game();
    return g != null && g.mindControlledPlayerId != null;
  }

  get mindControlledPlayerName(): string {
    const g = this.game();
    if (!g || !g.mindControlledPlayerId) return '';
    const idx = g.playerIds.indexOf(g.mindControlledPlayerId);
    return idx >= 0 ? g.playerNames[idx] : '';
  }

  get opponentHand(): Card[] {
    const g = this.game();
    if (this.isMindControlling) {
      return g?.hand ?? [];
    }
    return g?.opponentHand ?? [];
  }

  get hand(): Card[] {
    const g = this.game();
    if (this.isMindControlling) {
      return g?.opponentHand ?? [];
    }
    return g?.hand ?? [];
  }

  get player1DeckSize(): number {
    return this.game()?.deckSizes?.[0] ?? 0;
  }

  get player2DeckSize(): number {
    return this.game()?.deckSizes?.[1] ?? 0;
  }

  get player1HandSize(): number {
    return this.game()?.handSizes?.[0] ?? 0;
  }

  get player2HandSize(): number {
    return this.game()?.handSizes?.[1] ?? 0;
  }

  get myLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.myPlayerIndex] ?? 20;
  }

  get opponentLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.opponentPlayerIndex] ?? 20;
  }

  get manaPool(): Record<string, number> {
    return this.game()?.manaPool ?? {};
  }

  get totalMana(): number {
    return Object.values(this.manaPool).reduce((sum, v) => sum + v, 0);
  }

  get manaEntries(): { color: string; count: number }[] {
    return Object.entries(this.manaPool)
      .filter(([, count]) => count > 0)
      .map(([color, count]) => ({ color, count }));
  }

  get stackEntries(): StackEntry[] {
    return [...(this.game()?.stack ?? [])].reverse();
  }

  get isStackEmpty(): boolean {
    return (this.game()?.stack ?? []).length === 0;
  }

  getPlayerName(playerId: string): string {
    const g = this.game();
    if (!g) return '';
    const idx = g.playerIds.indexOf(playerId);
    return idx >= 0 ? g.playerNames[idx] : '';
  }

  getLifeTotal(playerIndex: number): number {
    return this.game()?.lifeTotals?.[playerIndex] ?? 20;
  }

  getPoisonCounters(playerIndex: number): number {
    return this.game()?.poisonCounters?.[playerIndex] ?? 0;
  }

  getPlayerId(playerIndex: number): string {
    return this.game()?.playerIds?.[playerIndex] ?? '';
  }

  // ========== Game state ==========

  private applyGameState(state: GameStateNotification): void {
    const g = this.game();
    if (!g) return;

    // Detect transition to RUNNING to clear mulligan UI state
    if (state.status === GameStatus.RUNNING && g.status !== GameStatus.RUNNING) {
      this.mulliganModal?.resetState();
    }

    const updated = {
      ...g,
      status: state.status,
      activePlayerId: state.activePlayerId,
      turnNumber: state.turnNumber,
      currentStep: state.currentStep,
      priorityPlayerId: state.priorityPlayerId,
      battlefields: state.battlefields,
      stack: state.stack,
      graveyards: state.graveyards,
      deckSizes: state.deckSizes,
      handSizes: state.handSizes,
      lifeTotals: state.lifeTotals,
      poisonCounters: state.poisonCounters,
      hand: state.hand,
      opponentHand: state.opponentHand ?? [],
      mulliganCount: state.mulliganCount,
      manaPool: state.manaPool,
      autoStopSteps: state.autoStopSteps,
      gameLog: [...g.gameLog, ...state.newLogEntries],
      mindControlledPlayerId: state.mindControlledPlayerId ?? null,
      revealedLibraryTopCards: state.revealedLibraryTopCards ?? []
    };
    this.game.set(updated);
    this.websocketService.currentGame = updated;

    this.playableCardIndices.set(new Set(state.playableCardIndices));
    this.potentialPlayableCardIndices.set(new Set(state.potentialPlayableCardIndices ?? []));
    this.potentialManaTotal.set(state.potentialManaTotal ?? 0);
    this.potentialPayableAbilityIndices.set(state.potentialPayableAbilityIndices ?? {});
    this.playableGraveyardLandIndices.set(new Set(state.playableGraveyardLandIndices ?? []));
    this.playableFlashbackIndices.set(new Set(state.playableFlashbackIndices ?? []));
    this.playableExileCards.set(state.playableExileCards ?? []);
    this.playableLibraryTopCards.set(state.playableLibraryTopCards ?? []);
    this.autoStopSteps.set(new Set(state.autoStopSteps));

    if (this.choice.awaitingXValueChoice && this.choice.xValueChoiceManaPayment) {
      this.choice.xValueChoiceMaxValue = this.totalMana;
    }
    if (this.choice.awaitingMayAbility) {
      this.choice.updateMayAbilityCanPay(state.manaPool);
    }
    this.searchTaxCost.set(state.searchTaxCost ?? 0);

    // Switch to stack tab when stack is non-empty
    if (state.stack.length > 0) {
      this.sidePanel?.switchToStackTab();
    } else {
      this.sidePanel?.switchToLogTabIfOnStack();
    }

    // Clear pending combat state when server confirms battlefield
    if (!this.declaringAttackers()) {
      this.selectedAttackerIndices.set(new Set());
      this.bandAssignments.set(new Map());
    }
    if (!this.declaringBlockers()) {
      this.blockerAssignments.set(new Map());
    }

    // MTGO-style casting: fire the held-back cast once the pool covers it
    this.choice.targeting.onGameStateUpdate();

    this.scheduleCombatShiftUpdate();
  }

  // ========== Mulligan ==========

  keepHand(): void {
    this.websocketService.send({ type: MessageType.KEEP_HAND });
  }

  takeMulligan(): void {
    this.websocketService.send({ type: MessageType.TAKE_MULLIGAN });
  }

  confirmBottomCards(cardIndices: number[]): void {
    this.websocketService.send({
      type: MessageType.BOTTOM_CARDS,
      cardIndices
    });
  }

  // ========== Priority & playability ==========

  playableCardIndices = signal(new Set<number>());
  potentialPlayableCardIndices = signal(new Set<number>());
  potentialManaTotal = signal(0);
  potentialPayableAbilityIndices = signal<Record<string, number[]>>({});
  playableGraveyardLandIndices = signal(new Set<number>());
  playableFlashbackIndices = signal(new Set<number>());
  playableExileCards = signal<Card[]>([]);
  playableLibraryTopCards = signal<Card[]>([]);
  autoStopSteps = signal(new Set<string>());
  searchTaxCost = signal(0);

  /** Clickable/highlighted in hand: strictly affordable now, or affordable once the
      player taps their mana sources (MTGO-style — clicking enters the payment flow). */
  isCardPlayable(index: number): boolean {
    return this.playableCardIndices().has(index) || this.potentialPlayableCardIndices().has(index);
  }

  isGraveyardLandPlayable(index: number): boolean {
    return this.playableGraveyardLandIndices().has(index);
  }

  playCard(index: number): void {
    if (this.choice.targeting.selectingAlternateCostHandCard) {
      this.choice.targeting.selectAlternateCostHandCard(index);
      return;
    }
    this.choice.targeting.playCard(index, (i) => this.isCardPlayable(i));
  }

  playGraveyardLand(index: number): void {
    if (this.isGraveyardLandPlayable(index)) {
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetId: null, fromGraveyard: true });
    }
  }

  isGraveyardAbilityActivatable(index: number): boolean {
    const card = this.myGraveyard[index];
    return card?.graveyardActivatedAbilities?.length > 0 && this.hasPriority;
  }

  activateGraveyardAbility(index: number): void {
    const card = this.myGraveyard[index];
    const ability = card?.graveyardActivatedAbilities?.[0];
    if (!ability) {
      return;
    }
    if (ability.manaCost?.includes('{X}')) {
      this.choice.targeting.startGraveyardXValue(index, ability);
      return;
    }
    if (ability.needsTarget) {
      this.choice.targeting.startGraveyardAbilityTargeting(index, ability);
      return;
    }
    this.websocketService.send({ type: MessageType.ACTIVATE_GRAVEYARD_ABILITY, graveyardCardIndex: index, abilityIndex: 0 });
  }

  isFlashbackPlayable(index: number): boolean {
    return this.playableFlashbackIndices().has(index);
  }

  playFlashback(index: number): void {
    if (this.isFlashbackPlayable(index)) {
      const card = this.myGraveyard[index];
      if (card?.needsTarget) {
        this.choice.targeting.startFlashbackTargeting(index, card);
      } else {
        this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetId: null, flashback: true });
      }
    }
  }

  playExileCard(card: Card): void {
    if (card.id) {
      this.choice.targeting.startExilePlay(card);
    }
  }

  get isLibraryTopPlayable(): boolean {
    return this.playableLibraryTopCards().length > 0;
  }

  playLibraryTopCard(): void {
    const card = this.playableLibraryTopCards()[0];
    if (card) {
      this.choice.targeting.startLibraryTopPlay(card);
    }
  }

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY });
    }
  }

  paySearchTax(): void {
    this.websocketService.send({ type: MessageType.PAY_SEARCH_TAX });
  }

  toggleAutoStop(step: string): void {
    if (this.isForceStop(step)) return;

    const g = this.game();
    if (!g) return;

    const current = new Set(this.autoStopSteps());
    if (current.has(step)) {
      current.delete(step);
    } else {
      current.add(step);
    }
    this.autoStopSteps.set(current);

    this.websocketService.send({
      type: MessageType.SET_AUTO_STOPS,
      stops: Array.from(current)
    });
  }

  isAutoStop(step: string): boolean {
    return this.autoStopSteps().has(step);
  }

  isForceStop(step: string): boolean {
    return step === TurnStep.PRECOMBAT_MAIN || step === TurnStep.POSTCOMBAT_MAIN;
  }

  // ========== Combat ==========

  declaringAttackers = signal(false);
  declaringBlockers = signal(false);
  /** True while this player is declaring blocks for creatures they do NOT control
      (Melee: "you choose which creatures block this combat"). Inverts which side of the
      board holds the blockers and which holds the attackers. */
  choosingBlocksForOpponent = signal(false);
  attackTaxPerCreature = signal(0);
  availableAttackerIndices = signal(new Set<number>());
  mustAttackIndices = signal(new Set<number>());
  availableBlockerIndices = signal(new Set<number>());
  selectedAttackerIndices = signal(new Set<number>());
  availableAttackTargets = signal<AttackTarget[]>([]);
  attackerTargetAssignments = signal(new Map<number, string>());
  /** Attacking-band grouping (CR 702.22): attacker index → band number (1-based). Absent = not in a band. */
  bandAssignments = signal(new Map<number, number>());
  opponentAttackerIndices = signal<number[]>([]);
  blockerAssignments = signal(new Map<number, number>());
  legalBlockPairs = signal(new Map<number, number[]>());
  selectedBlockerIndex = signal<number | null>(null);
  mustBeBlockedAttackerIndices = signal(new Set<number>());
  menaceAttackerIndices = signal(new Set<number>());
  mustBlockRequirements = signal(new Map<number, number[]>());
  gameOverActive = signal(false);
  gameOverWinner = signal<string | null>(null);
  gameOverWinnerId = signal<string | null>(null);
  showSurrenderConfirm = signal(false);
  mustAttackWithAtLeastOne = signal(false);

  private handleAvailableAttackers(msg: AvailableAttackersNotification): void {
    this.declaringAttackers.set(true);
    this.availableAttackerIndices.set(new Set(msg.attackerIndices));
    this.mustAttackIndices.set(new Set(msg.mustAttackIndices));
    this.selectedAttackerIndices.set(new Set(msg.mustAttackIndices));
    this.availableAttackTargets.set(msg.availableTargets || []);
    this.attackerTargetAssignments.set(new Map());
    this.bandAssignments.set(new Map());
    this.attackTaxPerCreature.set(msg.taxPerCreature || 0);
    this.mustAttackWithAtLeastOne.set(msg.mustAttackWithAtLeastOne || false);
  }

  private handleAvailableBlockers(msg: AvailableBlockersNotification): void {
    this.declaringBlockers.set(true);
    this.choosingBlocksForOpponent.set(msg.choosingForOpponent === true);
    this.availableBlockerIndices.set(new Set(msg.blockerIndices));
    this.opponentAttackerIndices.set(msg.attackerIndices);
    const pairs = new Map<number, number[]>();
    for (const [key, value] of Object.entries(msg.legalBlockPairs)) {
      pairs.set(Number(key), value);
    }
    this.legalBlockPairs.set(pairs);
    this.blockerAssignments.set(new Map());
    this.selectedBlockerIndex.set(null);
    this.mustBeBlockedAttackerIndices.set(new Set(msg.mustBeBlockedAttackerIndices || []));
    this.menaceAttackerIndices.set(new Set(msg.menaceAttackerIndices || []));
    const reqs = new Map<number, number[]>();
    for (const [key, value] of Object.entries(msg.mustBlockRequirements || {})) {
      reqs.set(Number(key), value);
    }
    this.mustBlockRequirements.set(reqs);
    this.scheduleCombatShiftUpdate();
  }

  private handleGameOver(msg: GameOverNotification): void {
    this.gameOverActive.set(true);
    this.gameOverWinner.set(msg.winnerName);
    this.gameOverWinnerId.set(msg.winnerId);
    this.choice.reset();
    const g = this.game();
    if (!g) return;
    const updated = { ...g, status: GameStatus.FINISHED };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  canAttack(index: number): boolean {
    return this.declaringAttackers() && this.availableAttackerIndices().has(index);
  }

  isSelectedAttacker(index: number): boolean {
    return this.selectedAttackerIndices().has(index);
  }

  toggleAttacker(index: number): void {
    if (!this.canAttack(index)) return;
    const updated = new Set(this.selectedAttackerIndices());
    if (updated.has(index)) {
      if (this.mustAttackIndices().has(index)) return;
      updated.delete(index);
      // Remove target and band assignment when deselecting
      const updatedTargets = new Map(this.attackerTargetAssignments());
      updatedTargets.delete(index);
      this.attackerTargetAssignments.set(updatedTargets);
      const updatedBands = new Map(this.bandAssignments());
      updatedBands.delete(index);
      this.bandAssignments.set(updatedBands);
    } else {
      updated.add(index);
    }
    this.selectedAttackerIndices.set(updated);
  }

  /**
   * Cycles the band grouping (CR 702.22) of a selected attacker: no band → Band A → Band B → …
   * → no band. Group two or more attackers under the same letter to attack as a band.
   */
  cycleBand(index: number): void {
    if (!this.selectedAttackerIndices().has(index) || !this.canDeclareBand()) return;
    const current = this.bandAssignments().get(index) ?? 0;
    const maxBand = Math.max(1, this.selectedAttackerIndices().size - 1);
    const next = current >= maxBand ? 0 : current + 1;
    const updated = new Map(this.bandAssignments());
    if (next === 0) {
      updated.delete(index);
    } else {
      updated.set(index, next);
    }
    this.bandAssignments.set(updated);
  }

  canDeclareBand(): boolean {
    return canFormAttackingBand(this.myBattlefield, this.selectedAttackerIndices());
  }

  /** Badge text for an attacker's band: "+ Band" when ungrouped, "Band A"/"Band B"/… when grouped. */
  getBandLabel(index: number): string {
    const band = this.bandAssignments().get(index);
    if (!band) return '+ Band';
    return 'Band ' + String.fromCharCode(64 + band);
  }

  /** Cycles through available attack targets for a selected attacker (player → planeswalker1 → ...) */
  cycleAttackTarget(index: number): void {
    const targets = this.availableAttackTargets();
    if (targets.length <= 1) return;
    const currentMap = this.attackerTargetAssignments();
    const currentTargetId = currentMap.get(index) || targets[0]?.id;
    const currentIdx = targets.findIndex(t => t.id === currentTargetId);
    const nextIdx = (currentIdx + 1) % targets.length;
    const updated = new Map(currentMap);
    updated.set(index, targets[nextIdx].id);
    this.attackerTargetAssignments.set(updated);
  }

  /** Returns the display name of the attack target for a given attacker */
  getAttackTargetName(index: number): string {
    const targets = this.availableAttackTargets();
    if (targets.length <= 1) return '';
    const targetId = this.attackerTargetAssignments().get(index) || targets[0]?.id;
    const target = targets.find(t => t.id === targetId);
    return target ? target.name : '';
  }

  confirmAttackers(): void {
    const g = this.game();
    if (!g) return;
    // Prevent empty declaration when forced to attack (e.g. Trove of Temptation)
    if (this.mustAttackWithAtLeastOne() && this.selectedAttackerIndices().size === 0) return;
    // Build attackTargets map if there are non-player targets (planeswalkers)
    const targets = this.availableAttackTargets();
    const hasPlaneswalkersTarget = targets.some(t => !t.isPlayer);
    const assignments = this.attackerTargetAssignments();
    const msg: Record<string, unknown> = {
      type: MessageType.DECLARE_ATTACKERS,
      attackerIndices: Array.from(this.selectedAttackerIndices())
    };
    if (hasPlaneswalkersTarget && assignments.size > 0) {
      const attackTargets: Record<number, string> = {};
      for (const [idx, targetId] of assignments) {
        attackTargets[idx] = targetId;
      }
      msg['attackTargets'] = attackTargets;
    }
    // Build attacking bands (CR 702.22): group selected attackers by band number, keeping only
    // groups of two or more. The engine validates band legality (>=1 banding, <=1 non-banding).
    const bandGroups = new Map<number, number[]>();
    for (const [idx, band] of this.bandAssignments()) {
      if (!this.selectedAttackerIndices().has(idx)) continue;
      const group = bandGroups.get(band) ?? [];
      group.push(idx);
      bandGroups.set(band, group);
    }
    const bands = Array.from(bandGroups.values()).filter(g => g.length >= 2);
    if (bands.length > 0) {
      msg['bands'] = bands;
    }
    this.websocketService.send(msg as unknown as WebSocketMessage);
    this.declaringAttackers.set(false);
    this.availableAttackerIndices.set(new Set());
    this.mustAttackIndices.set(new Set());
    this.availableAttackTargets.set([]);
    this.attackerTargetAssignments.set(new Map());
    this.bandAssignments.set(new Map());
    this.attackTaxPerCreature.set(0);
    this.mustAttackWithAtLeastOne.set(false);
  }

  canBlock(index: number): boolean {
    return this.declaringBlockers() && this.availableBlockerIndices().has(index);
  }

  /** Whether the given side of the board holds the blocking creatures for the current
      declaration. Normally the local player's side; inverted while choosing an opponent's blocks. */
  isBlockerSide(isMine: boolean): boolean {
    return isMine !== this.choosingBlocksForOpponent();
  }

  /** The complement of isBlockerSide: the side holding the attackers. */
  isAttackerSide(isMine: boolean): boolean {
    return !this.isBlockerSide(isMine);
  }

  private get blockerSideBattlefield(): Permanent[] {
    return this.choosingBlocksForOpponent() ? this.opponentBattlefield : this.myBattlefield;
  }

  private get attackerSideBattlefield(): Permanent[] {
    return this.choosingBlocksForOpponent() ? this.myBattlefield : this.opponentBattlefield;
  }

  isAssignedBlocker(index: number): boolean {
    return this.blockerAssignments().has(index);
  }

  selectBlocker(index: number): void {
    if (!this.canBlock(index)) return;
    if (this.blockerAssignments().has(index)) {
      const updated = new Map(this.blockerAssignments());
      updated.delete(index);
      this.blockerAssignments.set(updated);
      this.scheduleCombatShiftUpdate();
      return;
    }
    this.selectedBlockerIndex.set(index);
  }

  isBlockTarget(index: number): boolean {
    if (!this.declaringBlockers() || this.selectedBlockerIndex() === null) return false;
    if (!this.attackerSideBattlefield[index]?.attacking) return false;
    const legal = this.legalBlockPairs().get(this.selectedBlockerIndex()!);
    return legal != null && legal.includes(index);
  }

  assignBlock(attackerIndex: number): void {
    if (this.selectedBlockerIndex() === null || !this.declaringBlockers()) return;
    const perm = this.attackerSideBattlefield[attackerIndex];
    if (!perm || !perm.attacking) return;
    const legal = this.legalBlockPairs().get(this.selectedBlockerIndex()!);
    if (!legal || !legal.includes(attackerIndex)) return;
    const updated = new Map(this.blockerAssignments());
    updated.set(this.selectedBlockerIndex()!, attackerIndex);
    this.blockerAssignments.set(updated);
    this.selectedBlockerIndex.set(null);
    this.scheduleCombatShiftUpdate();
  }

  confirmBlockers(): void {
    const g = this.game();
    if (!g) return;
    const assignments = Array.from(this.blockerAssignments().entries()).map(([blockerIndex, attackerIndex]) => ({
      blockerIndex,
      attackerIndex
    }));
    this.websocketService.send({
      type: MessageType.DECLARE_BLOCKERS,
      blockerAssignments: assignments
    });
    this.declaringBlockers.set(false);
    this.choosingBlocksForOpponent.set(false);
    this.selectedBlockerIndex.set(null);
    this.availableBlockerIndices.set(new Set());
    this.legalBlockPairs.set(new Map());
    this.opponentAttackerIndices.set([]);
  }

  cancelBlockerSelection(): void {
    this.selectedBlockerIndex.set(null);
  }

  // ========== Battlefield display ==========

  getAttachedAuras(permanentId: string): AttachedAura[] {
    return getAttachedAuras(permanentId, this.myBattlefield, this.opponentBattlefield);
  }

  /** Face-up cards imprinted on / exiled with a permanent, shown tucked under it. */
  exiledWithCards(perm: Permanent): Card[] {
    return perm.exiledWithCards ?? [];
  }

  /** One entry per face-down card exiled with the permanent, for rendering card backs.
      Empty on the viewer's own permanents — those send the cards themselves instead. */
  faceDownExiledSlots(perm: Permanent): number[] {
    return Array.from({ length: perm.faceDownExiledCount ?? 0 }, (_, i) => i);
  }

  /** Face-down exiled cards revealed to their controller (opponents get card backs). */
  faceDownExiledCards(perm: Permanent): Card[] {
    return perm.faceDownExiledCards ?? [];
  }

  /** Total tucked-under cards (face-up + face-down) of a permanent, for the fit model. */
  private exiledWithCount(perm: Permanent): number {
    return (perm.exiledWithCards?.length ?? 0) + (perm.faceDownExiledCount ?? 0)
      + (perm.faceDownExiledCards?.length ?? 0);
  }

  onAuraClick(aura: AttachedAura): void {
    if (aura.isMine) {
      this.onMyBattlefieldCardClick(aura.originalIndex);
    } else {
      this.onOpponentBattlefieldCardClick(aura.originalIndex);
    }
  }

  get myLands(): IndexedPermanent[] {
    return splitBattlefield(this.myBattlefield).lands;
  }

  get opponentLands(): IndexedPermanent[] {
    return splitBattlefield(this.opponentBattlefield).lands;
  }

  get myLandStacks(): (IndexedPermanent | LandStack)[] {
    return stackBasicLands(this.myLands);
  }

  get opponentLandStacks(): (IndexedPermanent | LandStack)[] {
    return stackBasicLands(this.opponentLands);
  }

  isLandStack(item: IndexedPermanent | LandStack): item is LandStack {
    return isLandStack(item);
  }

  /** Prefixed so a stack's slot key can never collide with a land's permanent id. A stack
      is keyed by its slot rather than by its first land: keying on a member meant losing
      that member re-keyed the whole stack, rebuilding every card in it. */
  landStackTrackKey(item: IndexedPermanent | LandStack): string {
    return isLandStack(item) ? `stack:${item.key}` : `land:${item.perm.id}`;
  }

  isPermanentCreature(perm: Permanent): boolean {
    return isPermanentCreature(perm);
  }

  /* All creatures render in their own row; attacking/blocking ones stay in place
     and merely nudge toward the divider (see the combat CSS), so nothing is
     filtered out of the rows during combat. */
  myCreatures = computed(() => splitBattlefield(this.myBattlefield).creatures);
  opponentCreatures = computed(() => splitBattlefield(this.opponentBattlefield).creatures);

  // ========== Combat ==========

  /** True while attackers/blockers are being declared or are committed, so the
      divider turns red and combat creatures nudge toward it. */
  get inCombat(): boolean {
    if (this.declaringAttackers() || this.declaringBlockers()) return true;
    if (this.selectedAttackerIndices().size > 0 || this.blockerAssignments().size > 0) return true;
    return this.myBattlefield.some(p => p.attacking || p.blocking)
      || this.opponentBattlefield.some(p => p.attacking || p.blocking);
  }

  /** Whether the creature at the given index (on the given side) is attacking —
      either committed or currently being declared. */
  isAttackingCreature(index: number, isMine: boolean): boolean {
    const perm = (isMine ? this.myBattlefield : this.opponentBattlefield)[index];
    if (perm?.attacking) return true;
    return isMine && this.isSelectedAttacker(index);
  }

  /** Whether the creature at the given index (on the given side) is blocking —
      either committed or currently being assigned. */
  isBlockingCreature(index: number, isMine: boolean): boolean {
    const perm = (isMine ? this.myBattlefield : this.opponentBattlefield)[index];
    if (perm?.blocking) return true;
    return isMine && this.isAssignedBlocker(index);
  }

  // ========== Blocker alignment ==========

  /* Blockers slide horizontally to sit directly in front of the attacker they
     block (MTGO-style). The shift is pure transform (--card-shift-x), so the
     blocker keeps its layout slot and the fit/zoom model is unaffected.
     Positions are measured from the .permanent-stack wrappers, which never
     carry transforms, making the measurement stable and idempotent; the screen
     delta is divided by the blocker side's zoom because the transform runs
     inside the zoomed card. */
  /* Center-to-center spacing between co-blockers fanned under one attacker:
     a full card width plus the row gap, so they sit side by side instead of
     overlapping ("merging") each other. */
  private static readonly BLOCKER_SPREAD = BattlefieldFitModel.CARD_WIDTH + BattlefieldFitModel.ROW_GAP;

  private battlefieldAreaEl: HTMLElement | null = null;
  private combatShiftFrame: number | null = null;
  readonly combatShiftX = signal(new Map<string, number>());

  /** Style value for a creature's --card-shift-x, or null when unshifted. */
  combatShift(permId: string): string | null {
    const v = this.combatShiftX().get(permId);
    return v ? `${v}px` : null;
  }

  /* The summon dust (see .permanent-enter in shared-game-styles.css) is textured by a fixed
     SVG noise field, so every creature would otherwise land with a byte-identical cloud.
     These turn and mirror it per card, which moves the surviving clumps somewhere different
     each time without needing a second noise field. Bound only for creatures, which are the
     only permanents that raise dust.

     Keyed on the permanent's id rather than a random number so a given permanent always
     dusts the same way: change detection re-evaluates these on every cycle, and a random
     value would jitter the cloud mid-animation. */

  /** Per-creature rotation for the dust layers, -18deg to 18deg. */
  dustTurn(permId: string): string {
    return `${(GameComponent.dustHash(permId) % 37) - 18}deg`;
  }

  /** Per-creature mirror for the dust layers: 1 or -1, multiplied into their x scale. */
  dustFlip(permId: string): number {
    return GameComponent.dustHash(permId) & 1 ? -1 : 1;
  }

  /** FNV-1a, kept to 31 bits so the values above stay non-negative. */
  private static dustHash(permId: string): number {
    let h = 0x811c9dc5;
    for (let i = 0; i < permId.length; i++) {
      h = Math.imul(h ^ permId.charCodeAt(i), 0x01000193);
    }
    return h & 0x7fffffff;
  }

  /** Coalesces recomputes to one per frame, after layout has settled. */
  private scheduleCombatShiftUpdate(): void {
    if (this.combatShiftFrame != null) return;
    this.combatShiftFrame = requestAnimationFrame(() => {
      this.combatShiftFrame = null;
      this.updateCombatShifts();
    });
  }

  private updateCombatShifts(): void {
    const area = this.battlefieldAreaEl;
    const next = new Map<string, number>();
    if (area) {
      type Pair = { blockerId: string; attackerId: string; zoom: number };
      const pairs: Pair[] = [];
      // Local assignments while declaring (cleared once the server state lands)
      for (const [bIdx, aIdx] of this.blockerAssignments()) {
        const b = this.myBattlefield[bIdx];
        const a = this.opponentBattlefield[aIdx];
        if (b && a) pairs.push({ blockerId: b.id, attackerId: a.id, zoom: this.myBattlefieldZoom });
      }
      // Committed blocks from the game state, either side
      const addCommitted = (own: Permanent[], enemy: Permanent[], zoom: number) => {
        for (const p of own) {
          if (p.blocking && p.blockingTargets?.length > 0) {
            const a = enemy[p.blockingTargets[0]];
            if (a && !pairs.some(pr => pr.blockerId === p.id)) {
              pairs.push({ blockerId: p.id, attackerId: a.id, zoom });
            }
          }
        }
      };
      addCommitted(this.myBattlefield, this.opponentBattlefield, this.myBattlefieldZoom);
      addCommitted(this.opponentBattlefield, this.myBattlefield, this.opponentBattlefieldZoom);

      const centerX = (id: string): number | null => {
        const el = area.querySelector(`[data-combat-id="${id}"]`);
        if (!el) return null;
        const rect = el.getBoundingClientRect();
        return rect.left + rect.width / 2;
      };

      const byAttacker = new Map<string, Pair[]>();
      for (const pr of pairs) {
        const group = byAttacker.get(pr.attackerId);
        if (group) group.push(pr); else byAttacker.set(pr.attackerId, [pr]);
      }
      for (const [attackerId, group] of byAttacker) {
        const aCx = centerX(attackerId);
        if (aCx == null) continue;
        // Multiple blockers fan out side by side under their attacker,
        // ordered by their natural row position to avoid crossing paths.
        const blockers = group
          .map(pr => ({ pr, cx: centerX(pr.blockerId) }))
          .filter((b): b is { pr: Pair; cx: number } => b.cx != null)
          .sort((x, y) => x.cx - y.cx);
        blockers.forEach((b, i) => {
          const slot = (i - (blockers.length - 1) / 2) * GameComponent.BLOCKER_SPREAD * b.pr.zoom;
          next.set(b.pr.blockerId, Math.round((aCx + slot - b.cx) / b.pr.zoom));
        });
      }

      this.resolveRowOverlaps(this.myCreatures(), this.myBattlefieldZoom, next, area);
      this.resolveRowOverlaps(this.opponentCreatures(), this.opponentBattlefieldZoom, next, area);
    }
    const current = this.combatShiftX();
    if (next.size !== current.size || [...next].some(([k, v]) => current.get(k) !== v)) {
      this.combatShiftX.set(next);
    }
  }

  /** After blockers align with their attackers, push aside any same-line
      creature an aligned blocker would come to rest on, cascading so a pushed
      card can't just land on the next one — no two cards end up on top of
      each other. Works in screen px on the transform-free stack rects; pushes
      go into the same local-px shift map as the blocker alignment. */
  private resolveRowOverlaps(creatures: IndexedPermanent[], zoom: number, next: Map<string, number>, area: HTMLElement): void {
    const gap = BattlefieldFitModel.ROW_GAP * zoom;
    type Box = { id: string; left: number; right: number; top: number; bottom: number; aligned: boolean };
    const boxes: Box[] = [];
    for (const ip of creatures) {
      const el = area.querySelector(`[data-combat-id="${ip.perm.id}"]`);
      if (!el) continue;
      const rect = el.getBoundingClientRect();
      const shift = (next.get(ip.perm.id) ?? 0) * zoom;
      boxes.push({
        id: ip.perm.id,
        left: rect.left + shift,
        right: rect.right + shift,
        top: rect.top,
        bottom: rect.bottom,
        aligned: next.has(ip.perm.id)
      });
    }
    if (!boxes.some(b => b.aligned)) return;

    const push = new Map<string, number>();
    const pos = (b: Box) => {
      const p = push.get(b.id) ?? 0;
      return { left: b.left + p, right: b.right + p, moved: b.aligned || p !== 0 };
    };
    for (let pass = 0, changed = true; pass < 4 && changed; pass++) {
      changed = false;
      for (const m of boxes) {
        if (m.aligned) continue;
        const mp = pos(m);
        for (const o of boxes) {
          if (o.id === m.id) continue;
          const op = pos(o);
          // Only moved cards repel; untouched neighbours are already spaced.
          if (!op.moved) continue;
          const sameLine = m.top < o.bottom && m.bottom > o.top;
          if (!sameLine || mp.left >= op.right + gap || mp.right <= op.left - gap) continue;
          const escapesLeft = (mp.left + mp.right) / 2 <= (op.left + op.right) / 2;
          push.set(m.id, escapesLeft ? op.left - gap - m.right : op.right + gap - m.left);
          changed = true;
          break;
        }
      }
    }
    for (const [id, p] of push) {
      const local = Math.round(p / zoom);
      if (local !== 0) next.set(id, local);
    }
  }

  /** During blocker declaration: whether a blocker is already assigned to the
      opponent's attacker at the given index. */
  isAttackerBlocked(attackerIndex: number): boolean {
    for (const assigned of this.blockerAssignments().values()) {
      if (assigned === attackerIndex) return true;
    }
    return false;
  }

  /** Badge text for a blocking creature: names the blocked attacker(s) — from
      the local assignment while declaring, from the committed combat state
      (blockingTargets index into the attacker's battlefield) afterwards. */
  getBlockingBadgeText(index: number, isMine: boolean): string {
    const own = isMine ? this.myBattlefield : this.opponentBattlefield;
    const enemy = isMine ? this.opponentBattlefield : this.myBattlefield;
    if (this.declaringBlockers() && this.isBlockerSide(isMine)) {
      const attackerIndex = this.blockerAssignments().get(index);
      const name = attackerIndex != null ? enemy[attackerIndex]?.card.name : null;
      return name ? `Blocks ${name}` : 'Blocking';
    }
    const names = (own[index]?.blockingTargets ?? [])
      .map(t => enemy[t]?.card.name)
      .filter(n => n != null);
    return names.length > 0 ? `Blocks ${names.join(', ')}` : 'Blocking';
  }

  // ========== Permanent rendering ==========

  /** Context for the #permanentStack template, which renders every permanent on the
      board from one definition. `mine` picks our own click/tap/ability behaviour over
      the opponent's; `creature` turns on the combat classes, badges and the
      blocker-alignment shift that only creatures use. */
  stackCtx(ip: IndexedPermanent, mine: boolean, creature: boolean): PermanentStackContext {
    return { $implicit: ip, mine, creature };
  }

  onPermanentClick(ip: IndexedPermanent, mine: boolean, event: MouseEvent): void {
    if (mine) {
      this.onMyBattlefieldCardClick(ip.originalIndex, event);
    } else {
      this.onOpponentBattlefieldCardClick(ip.originalIndex);
    }
  }

  /** Right-click clears a creature's assigned combat damage. Only creatures ever carry
      an assignment, so lands keep the browser's own context menu. */
  onPermanentContextMenu(event: MouseEvent, ip: IndexedPermanent, creature: boolean): void {
    if (!creature) return;
    event.preventDefault();
    this.choice.damage.unassignDamage(ip.perm.id);
  }

  // ========== Click dispatch ==========

  private readonly attackingCreatureFilter = (p: Permanent) => isPermanentCreature(p) && p.attacking;

  onMyBattlefieldCardClick(index: number, event?: MouseEvent): void {
    const perm = this.myBattlefield[index];
    if (this.clickResolver.tryResolveClick(perm, this.attackingCreatureFilter)) return;
    if (this.choice.targeting.convoking) {
      if (perm && isPermanentCreature(perm) && !perm.tapped) {
        this.choice.targeting.toggleConvokeCreature(perm.id);
      }
      return;
    }
    if (this.choice.targeting.choosingKickerPermanent) {
      if (perm && !perm.tapped) {
        this.choice.targeting.toggleKickerPermanent(perm.id);
      }
      return;
    }
    if (this.choice.targeting.choosingBuybackSacrifice) {
      if (perm && isPermanentLand(perm)) {
        this.choice.targeting.toggleBuybackSacrifice(perm.id);
      }
      return;
    }
    if (this.choice.targeting.selectingAlternateCostCreatures) {
      if (perm) {
        const canSelectCreature = this.choice.targeting.alternateCostSacrificeCount > 0 && isPermanentCreature(perm);
        const canSelectArtifact = this.choice.targeting.alternateCostTapCount > 0 && isPermanentArtifact(perm) && !perm.tapped;
        const canSelectLand = this.choice.targeting.alternateCostReturnCount > 0 && isPermanentLand(perm);
        if (canSelectCreature || canSelectArtifact || canSelectLand) {
          this.choice.targeting.toggleAlternateCostCreature(perm.id);
        }
      }
      return;
    }
    if (this.choice.awaitingXValueChoice || (this.choice.awaitingMayAbility && this.choice.mayAbilityManaCost != null)) {
      if (perm && this.choice.canTapForMana(perm)) {
        this.tapPermanentForMana(index, perm);
      }
      return;
    }
    if (this.declaringAttackers()) {
      // CR 508.1i: allow tapping mana sources to pay attack tax
      if (this.attackTaxPerCreature() > 0 && perm && !this.canAttack(index) && this.canTapPermanentForMana(perm)) {
        this.tapPermanentForMana(index, perm);
        return;
      }
      this.toggleAttacker(index);
    } else if (this.declaringBlockers()) {
      if (this.isBlockerSide(true)) {
        this.selectBlocker(index);
      } else {
        this.assignBlock(index);
      }
    } else {
      this.choice.targeting.tapPermanent(index);
      if (this.choice.targeting.choosingAbility) {
        event?.stopPropagation();
      }
    }
  }

  onOpponentBattlefieldCardClick(index: number): void {
    const perm = this.opponentBattlefield[index];
    if (this.clickResolver.tryResolveClick(perm, this.attackingCreatureFilter)) return;
    if (this.declaringBlockers()) {
      if (this.isBlockerSide(false)) {
        this.selectBlocker(index);
      } else {
        this.assignBlock(index);
      }
    }
  }

  // ========== Attack tax mana helpers ==========

  canTapPermanentForMana(perm: Permanent): boolean {
    if (perm.tapped) return false;
    if (perm.summoningSick && isPermanentCreature(perm)) return false;
    if (perm.card.hasTapAbility) return true;
    return perm.card.activatedAbilities.some(a => a.isManaAbility);
  }

  private tapPermanentForMana(index: number, perm: Permanent): void {
    // Prefer the intrinsic ON_TAP mana (a basic land's own color) over granted/activated
    // mana abilities so e.g. a Plains that also gained "{T}: Add {U}" still pays white
    if (perm.card.hasTapAbility && !perm.tapped) {
      this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
      return;
    }
    const manaAbilityIndex = perm.card.activatedAbilities.findIndex(a => a.isManaAbility);
    if (manaAbilityIndex >= 0) {
      this.websocketService.send({ type: MessageType.ACTIVATE_ABILITY, permanentIndex: index, abilityIndex: manaAbilityIndex });
    } else {
      this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
    }
  }

  // ========== Stack display ==========

  onStackEntryHover(entry: StackEntry): void {
    this.stackTargetId.set(entry.targetId);
  }

  onStackEntryHoverEnd(): void {
    this.stackTargetId.set(null);
  }

  onPlayerBadgeClick(playerIndex: number): void {
    const playerId = this.getPlayerId(playerIndex);
    if (this.choice.choosingPermanent && (this.choice.choosablePermanentIds().has(playerId) || this.choice.choosablePlayerIds().has(playerId))) {
      this.choice.choosePermanent(playerId);
    } else if (this.choice.targeting.multiTargeting && this.choice.targeting.validTargetPlayerIds().size > 0) {
      if (this.choice.targeting.isMultiTargetSelected(playerId)) {
        this.choice.targeting.removeMultiTarget(playerId);
      } else {
        this.choice.targeting.addMultiTargetPlayer(playerIndex);
      }
    } else {
      this.choice.targeting.selectPlayerTarget(playerIndex);
    }
  }

  getStackEntryTargetName(entry: StackEntry): string | null {
    if (!entry.targetId) return null;
    const g = this.game();
    if (!g) return null;
    const playerIdx = g.playerIds.indexOf(entry.targetId);
    if (playerIdx >= 0) return g.playerNames[playerIdx];
    for (const bf of g.battlefields) {
      for (const perm of bf) {
        if (perm.id === entry.targetId) return perm.card.name;
      }
    }
    for (const se of g.stack) {
      if (se.cardId === entry.targetId) return se.card.name;
    }
    for (const graveyard of g.graveyards) {
      for (const card of graveyard) {
        if (card.id === entry.targetId) return card.name;
      }
    }
    return null;
  }

  // ========== Hover & navigation ==========

  onSurrenderClick(): void {
    this.showSurrenderConfirm.set(true);
  }

  confirmSurrender(): void {
    this.showSurrenderConfirm.set(false);
    this.websocketService.send({ type: MessageType.SURRENDER });
  }

  cancelSurrender(): void {
    this.showSurrenderConfirm.set(false);
  }

  backToLobby(): void {
    this.websocketService.currentGame = null;
    // Tell the backend we're leaving the game, which returns us to lobby status
    this.websocketService.send({ type: MessageType.LEAVE_GAME });

    if (this.websocketService.inDraft) {
      this.router.navigate(['/draft']);
    } else {
      this.router.navigate(['/home']);
    }
  }

  onCardHover(card: Card, permanent: Permanent | null = null, event?: MouseEvent): void {
    this.hoveredCard.set(card);
    this.hoveredPermanent.set(permanent);
    this.clearModifierTooltip();
    if (permanent && event) {
      const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
      // Not enough room above the card (opponent's rows at the top) — flip under it.
      const below = rect.top < 240;
      const halfTooltipWidth = 120;
      const anchor = {
        x: Math.min(Math.max(rect.left + rect.width / 2, halfTooltipWidth), window.innerWidth - halfTooltipWidth),
        y: below ? rect.bottom + 6 : rect.top - 6,
        below,
      };
      this.modifierTooltipTimer = setTimeout(() => {
        this.modifierTooltipTimer = null;
        this.modifierTooltipAnchor.set(anchor);
      }, 3000);
    }
  }

  onCardHoverEnd(): void {
    this.hoveredCard.set(null);
    this.hoveredPermanent.set(null);
    this.clearModifierTooltip();
  }

  private clearModifierTooltip(): void {
    if (this.modifierTooltipTimer != null) {
      clearTimeout(this.modifierTooltipTimer);
      this.modifierTooltipTimer = null;
    }
    this.modifierTooltipAnchor.set(null);
  }

  // ========== Keyboard shortcuts ==========

  showShortcutsPopup = signal(false);

  toggleShortcutsPopup(event: MouseEvent): void {
    event.stopPropagation();
    this.showShortcutsPopup.update(v => !v);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.showShortcutsPopup.set(false);
  }

  // Position of the draggable revealed-hand window; null keeps it default-centered.
  readonly revealHandPos = signal<{ x: number; y: number } | null>(null);
  private revealHandDragOffset: { x: number; y: number } | null = null;

  /** Begin dragging the revealed-hand window from its title bar. */
  startRevealHandDrag(event: MouseEvent): void {
    event.preventDefault();
    const window = (event.currentTarget as HTMLElement).closest('.reveal-hand-window') as HTMLElement | null;
    if (!window) return;
    // Seed from the current on-screen rect so the first drag doesn't jump.
    if (!this.revealHandPos()) {
      const rect = window.getBoundingClientRect();
      this.revealHandPos.set({ x: rect.left, y: rect.top });
    }
    const pos = this.revealHandPos()!;
    this.revealHandDragOffset = { x: event.clientX - pos.x, y: event.clientY - pos.y };
    document.addEventListener('mousemove', this.onRevealHandDragMove);
    document.addEventListener('mouseup', this.onRevealHandDragEnd);
  }

  private onRevealHandDragMove = (event: MouseEvent): void => {
    if (!this.revealHandDragOffset) return;
    this.revealHandPos.set({
      x: event.clientX - this.revealHandDragOffset.x,
      y: event.clientY - this.revealHandDragOffset.y
    });
  };

  private onRevealHandDragEnd = (): void => {
    this.revealHandDragOffset = null;
    document.removeEventListener('mousemove', this.onRevealHandDragMove);
    document.removeEventListener('mouseup', this.onRevealHandDragEnd);
  };

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (this.connectionLost()) return;
    if (this.game()?.status !== GameStatus.RUNNING) return;
    const target = event.target as HTMLElement | null;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.tagName === 'SELECT' || target.isContentEditable)) return;

    if (event.key === 'Escape') {
      if (this.handleEscape()) {
        event.preventDefault();
      }
      return;
    }

    if (event.key === ' ' || event.key === 'Enter') {
      // A focused button already handles Space/Enter natively — don't double-fire.
      if (target && target.tagName === 'BUTTON') return;
      event.preventDefault();
      if (event.repeat) return;
      this.handlePrimaryAction();
    }
  }

  /** Space/Enter: the action the big side-panel button would perform. */
  private handlePrimaryAction(): void {
    if (this.isChoicePending) return;
    if (this.declaringAttackers()) {
      this.confirmAttackers();
    } else if (this.declaringBlockers()) {
      this.confirmBlockers();
    } else if (this.hasPriority) {
      this.passPriority();
    }
  }

  /** Esc: back out of the innermost cancelable interaction. Returns whether it consumed the key. */
  private handleEscape(): boolean {
    const t = this.choice.targeting;
    if (this.showShortcutsPopup()) { this.showShortcutsPopup.set(false); return true; }
    if (this.showSurrenderConfirm()) { this.cancelSurrender(); return true; }
    if (t.payingForCast) { t.cancelPendingCast(); return true; }
    if (t.payingForAbility) { t.cancelPendingAbility(); return true; }
    if (t.choosingAbility) { t.cancelAbilityChoice(); return true; }
    if (t.choosingMode) { t.cancelModes(); return true; }
    if (t.choosingKickerPermanent) { t.cancelKickerPermanent(); return true; }
    if (t.choosingKicker) { t.cancelKicker(); return true; }
    if (t.choosingBuyback) { t.cancelBuyback(); return true; }
    if (t.choosingPhyrexianPayment) { t.cancelPhyrexianPayment(); return true; }
    if (t.choosingAlternateCost || t.selectingAlternateCostCreatures || t.selectingAlternateCostHandCard) { t.cancelAlternateCost(); return true; }
    if (t.choosingXValue) { t.cancelXValue(); return true; }
    if (t.convoking) { t.cancelConvoke(); return true; }
    if (t.targetingGraveyard) { t.cancelGraveyardTargeting(); return true; }
    if (t.multiTargeting) { t.cancelMultiTargeting(); return true; }
    if (t.targetingSpell) { t.cancelSpellTargeting(); return true; }
    if (t.selectingTarget) { t.cancelTargeting(); return true; }
    if (this.declaringBlockers() && this.selectedBlockerIndex() !== null) { this.cancelBlockerSelection(); return true; }
    return false;
  }

  /** Any pending choice/targeting flow the server or UI is waiting on — Space
      must not pass priority (or confirm combat) out from under it. */
  private get isChoicePending(): boolean {
    const c = this.choice;
    const t = c.targeting;
    return c.choosingFromHand || c.choosingFromList || c.awaitingMayAbility
      || c.choosingPermanent || c.choosingMultiplePermanents || c.choosingGraveyardCards
      || c.revealingHand || c.choosingFromGraveyard || c.awaitingXValueChoice
      || c.library.scrying || c.library.reorderingLibrary || c.library.searchingLibrary || c.library.choosingHandTopBottom
      || c.damage.assigningCombatDamage || c.damage.distributingDamage
      || t.selectingTarget || t.targetingSpell || t.multiTargeting || t.convoking || t.payingForCast || t.payingForAbility
      || t.choosingAbility || t.choosingXValue || t.choosingMode || t.choosingKicker || t.choosingKickerPermanent || t.choosingBuyback
      || t.choosingPhyrexianPayment || t.choosingAlternateCost || t.selectingAlternateCostCreatures
      || t.selectingAlternateCostHandCard
      || t.targetingGraveyard;
  }

  // ========== Formatting ==========

  formatAbilityDescription(description: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(manaSymbolHtml(description));
  }

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
