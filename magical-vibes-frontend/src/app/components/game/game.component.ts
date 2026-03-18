import { Component, OnInit, OnDestroy, ViewChild, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { WebsocketService, WebSocketMessage, Game, GameNotification, GameStateNotification, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, Permanent, MulliganResolvedNotification, SelectCardsToBottomNotification, AttackTarget, AvailableAttackersNotification, AvailableBlockersNotification, GameOverNotification, ChooseCardFromHandNotification, ChooseFromListNotification, MayAbilityNotification, ChoosePermanentNotification, ChooseMultiplePermanentsNotification, ChooseMultipleCardsFromGraveyardsNotification, StackEntry, ScryNotification, ReorderLibraryCardsNotification, ChooseCardFromLibraryNotification, RevealHandNotification, ChooseFromRevealedHandNotification, ChooseCardFromGraveyardNotification, ChooseHandTopBottomNotification, CombatDamageAssignmentNotification, ValidTargetsResponse, XValueChoiceNotification } from '../../services/websocket.service';
import { GameChoiceService } from '../../services/game-choice.service';
import { CardDisplayComponent } from './card-display/card-display.component';
import { MulliganModalComponent } from './mulligan-modal/mulligan-modal.component';
import { CombatZoneComponent } from './combat-zone/combat-zone.component';
import { SidePanelComponent } from './side-panel/side-panel.component';
import { IndexedPermanent, CombatGroup, CombatBlocker, AttachedAura, LandStack, splitBattlefield, stackBasicLands, getAttachedAuras, isLandStack, isPermanentCreature } from './battlefield.utils';
import { Subscription } from 'rxjs';
import { ManaSymbolService } from '../../services/mana-symbol.service';
import { PermanentClickResolverService } from '../../services/permanent-click-resolver.service';

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule, FormsModule, CardDisplayComponent, MulliganModalComponent, CombatZoneComponent, SidePanelComponent],
  templateUrl: './game.component.html',
  styleUrls: ['./shared-game-styles.css', './game.component.css']
})
export class GameComponent implements OnInit, OnDestroy {
  game = signal<Game | null>(null);
  hoveredCard = signal<Card | null>(null);
  hoveredPermanent = signal<Permanent | null>(null);
  stackTargetId = signal<string | null>(null);
  private subscriptions: Subscription[] = [];

  @ViewChild(MulliganModalComponent) mulliganModal?: MulliganModalComponent;
  @ViewChild(SidePanelComponent) sidePanel?: SidePanelComponent;

  readonly choice = inject(GameChoiceService);
  private clickResolver = inject(PermanentClickResolverService);
  private manaSymbolService = inject(ManaSymbolService);
  private sanitizer = inject(DomSanitizer);

  // Bound function references for child component inputs
  readonly boundIsValidTarget = (perm: Permanent) => this.choice.targeting.isValidTarget(perm);
  readonly boundIsSelectedAttacker = (index: number) => this.isSelectedAttacker(index);
  readonly boundGetAttachedAuras = (permanentId: string) => this.getAttachedAuras(permanentId);
  readonly boundGetDamageAssigned = (permanentId: string) => this.choice.damage.getDamageAssigned(permanentId);
  readonly boundUnassignDamage = (permanentId: string) => this.choice.damage.unassignDamage(permanentId);
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
    this.gameOverWinner.set(null);
    this.gameOverWinnerId.set(null);
    this.declaringAttackers.set(false);
    this.declaringBlockers.set(false);
    this.availableAttackerIndices.set(new Set());
    this.mustAttackIndices.set(new Set());
    this.availableBlockerIndices.set(new Set());
    this.selectedAttackerIndices.set(new Set());
    this.opponentAttackerIndices.set([]);
    this.blockerAssignments.set(new Map());
    this.legalBlockPairs.set(new Map());
    this.selectedBlockerIndex.set(null);
    this.playableCardIndices.set(new Set());
    this.playableGraveyardLandIndices.set(new Set());
    this.playableFlashbackIndices.set(new Set());
    this.playableExileCards.set([]);
    this.searchTaxCost.set(0);
    this.hoveredCard.set(null);
    this.hoveredPermanent.set(null);
    this.stackTargetId.set(null);

    this.choice.init(
      this.game,
      () => this.myBattlefield,
      () => this.opponentBattlefield,
      () => this.totalMana
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
        this.router.navigate(['/']);
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private processGameMessage(message: WebSocketMessage): void {
    console.log(message);

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

    if (message.type === MessageType.CHOOSE_CARD_FROM_HAND) {
      this.choice.handleChooseCardFromHand(message as ChooseCardFromHandNotification);
    }

    if (message.type === MessageType.CHOOSE_FROM_LIST) {
      this.choice.handleChooseFromList(message as ChooseFromListNotification);
    }

    if (message.type === MessageType.MAY_ABILITY_CHOICE) {
      this.choice.handleMayAbilityChoice(message as MayAbilityNotification);
    }

    if (message.type === MessageType.CHOOSE_PERMANENT) {
      this.choice.handleChoosePermanent(message as ChoosePermanentNotification);
    }

    if (message.type === MessageType.CHOOSE_MULTIPLE_PERMANENTS) {
      this.choice.handleChooseMultiplePermanents(message as ChooseMultiplePermanentsNotification);
    }

    if (message.type === MessageType.CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS) {
      this.choice.handleChooseMultipleCardsFromGraveyards(message as ChooseMultipleCardsFromGraveyardsNotification);
    }

    if (message.type === MessageType.SCRY) {
      this.choice.handleScry(message as ScryNotification);
    }

    if (message.type === MessageType.REORDER_LIBRARY_CARDS) {
      this.choice.handleReorderLibraryCards(message as ReorderLibraryCardsNotification);
    }

    if (message.type === MessageType.CHOOSE_CARD_FROM_LIBRARY) {
      this.choice.handleChooseCardFromLibrary(message as ChooseCardFromLibraryNotification);
    }

    if (message.type === MessageType.CHOOSE_HAND_TOP_BOTTOM) {
      this.choice.handleChooseHandTopBottom(message as ChooseHandTopBottomNotification);
    }

    if (message.type === MessageType.REVEAL_HAND) {
      this.choice.handleRevealHand(message as RevealHandNotification);
    }

    if (message.type === MessageType.CHOOSE_FROM_REVEALED_HAND) {
      this.choice.handleChooseFromRevealedHand(message as ChooseFromRevealedHandNotification);
    }

    if (message.type === MessageType.CHOOSE_CARD_FROM_GRAVEYARD) {
      this.choice.handleChooseCardFromGraveyard(message as ChooseCardFromGraveyardNotification);
    }

    if (message.type === MessageType.COMBAT_DAMAGE_ASSIGNMENT) {
      this.choice.handleCombatDamageAssignment(message as CombatDamageAssignmentNotification);
    }

    if (message.type === MessageType.VALID_TARGETS_RESPONSE) {
      this.choice.handleValidTargetsResponse(message as ValidTargetsResponse);
    }

    if (message.type === MessageType.X_VALUE_CHOICE) {
      this.choice.handleXValueChoice(message as XValueChoiceNotification);
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

  get gameLog(): string[] {
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
    this.playableGraveyardLandIndices.set(new Set(state.playableGraveyardLandIndices ?? []));
    this.playableFlashbackIndices.set(new Set(state.playableFlashbackIndices ?? []));
    this.playableExileCards.set(state.playableExileCards ?? []);
    this.autoStopSteps.set(new Set(state.autoStopSteps));

    if (this.choice.awaitingXValueChoice) {
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
      this.sidePanel?.switchToGameTabIfOnStack();
    }

    // Clear pending combat state when server confirms battlefield
    if (!this.declaringAttackers()) {
      this.selectedAttackerIndices.set(new Set());
    }
    if (!this.declaringBlockers()) {
      this.blockerAssignments.set(new Map());
    }
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
  playableGraveyardLandIndices = signal(new Set<number>());
  playableFlashbackIndices = signal(new Set<number>());
  playableExileCards = signal<Card[]>([]);
  autoStopSteps = signal(new Set<string>());
  searchTaxCost = signal(0);

  isCardPlayable(index: number): boolean {
    return this.playableCardIndices().has(index);
  }

  isGraveyardLandPlayable(index: number): boolean {
    return this.playableGraveyardLandIndices().has(index);
  }

  playCard(index: number): void {
    this.choice.targeting.playCard(index, (i) => this.isCardPlayable(i));
  }

  playGraveyardLand(index: number): void {
    if (this.isGraveyardLandPlayable(index)) {
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetPermanentId: null, fromGraveyard: true });
    }
  }

  isGraveyardAbilityActivatable(index: number): boolean {
    const card = this.myGraveyard[index];
    return card?.graveyardActivatedAbilities?.length > 0 && this.hasPriority;
  }

  activateGraveyardAbility(index: number): void {
    const card = this.myGraveyard[index];
    if (card?.graveyardActivatedAbilities?.length > 0) {
      this.websocketService.send({ type: MessageType.ACTIVATE_GRAVEYARD_ABILITY, graveyardCardIndex: index, abilityIndex: 0 });
    }
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
        this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: index, targetPermanentId: null, flashback: true });
      }
    }
  }

  playExileCard(card: Card): void {
    if (card.id) {
      this.websocketService.send({ type: MessageType.PLAY_CARD, cardIndex: 0, targetPermanentId: null, fromExileCardId: card.id });
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
  availableAttackerIndices = signal(new Set<number>());
  mustAttackIndices = signal(new Set<number>());
  availableBlockerIndices = signal(new Set<number>());
  selectedAttackerIndices = signal(new Set<number>());
  availableAttackTargets = signal<AttackTarget[]>([]);
  attackerTargetAssignments = signal(new Map<number, string>());
  opponentAttackerIndices = signal<number[]>([]);
  blockerAssignments = signal(new Map<number, number>());
  legalBlockPairs = signal(new Map<number, number[]>());
  selectedBlockerIndex = signal<number | null>(null);
  gameOverWinner = signal<string | null>(null);
  gameOverWinnerId = signal<string | null>(null);
  showSurrenderConfirm = signal(false);

  private handleAvailableAttackers(msg: AvailableAttackersNotification): void {
    this.declaringAttackers.set(true);
    this.availableAttackerIndices.set(new Set(msg.attackerIndices));
    this.mustAttackIndices.set(new Set(msg.mustAttackIndices));
    this.selectedAttackerIndices.set(new Set(msg.mustAttackIndices));
    this.availableAttackTargets.set(msg.availableTargets || []);
    this.attackerTargetAssignments.set(new Map());
  }

  private handleAvailableBlockers(msg: AvailableBlockersNotification): void {
    this.declaringBlockers.set(true);
    this.availableBlockerIndices.set(new Set(msg.blockerIndices));
    this.opponentAttackerIndices.set(msg.attackerIndices);
    const pairs = new Map<number, number[]>();
    for (const [key, value] of Object.entries(msg.legalBlockPairs)) {
      pairs.set(Number(key), value);
    }
    this.legalBlockPairs.set(pairs);
    this.blockerAssignments.set(new Map());
    this.selectedBlockerIndex.set(null);
  }

  private handleGameOver(msg: GameOverNotification): void {
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
      // Remove target assignment when deselecting
      const updatedTargets = new Map(this.attackerTargetAssignments());
      updatedTargets.delete(index);
      this.attackerTargetAssignments.set(updatedTargets);
    } else {
      updated.add(index);
    }
    this.selectedAttackerIndices.set(updated);
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
    this.websocketService.send(msg as unknown as WebSocketMessage);
    this.declaringAttackers.set(false);
    this.availableAttackerIndices.set(new Set());
    this.mustAttackIndices.set(new Set());
    this.availableAttackTargets.set([]);
    this.attackerTargetAssignments.set(new Map());
  }

  canBlock(index: number): boolean {
    return this.declaringBlockers() && this.availableBlockerIndices().has(index);
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
      return;
    }
    this.selectedBlockerIndex.set(index);
  }

  isBlockTarget(index: number): boolean {
    if (!this.declaringBlockers() || this.selectedBlockerIndex() === null) return false;
    if (!this.opponentBattlefield[index]?.attacking) return false;
    const legal = this.legalBlockPairs().get(this.selectedBlockerIndex()!);
    return legal != null && legal.includes(index);
  }

  assignBlock(attackerIndex: number): void {
    if (this.selectedBlockerIndex() === null || !this.declaringBlockers()) return;
    const perm = this.opponentBattlefield[attackerIndex];
    if (!perm || !perm.attacking) return;
    const legal = this.legalBlockPairs().get(this.selectedBlockerIndex()!);
    if (!legal || !legal.includes(attackerIndex)) return;
    const updated = new Map(this.blockerAssignments());
    updated.set(this.selectedBlockerIndex()!, attackerIndex);
    this.blockerAssignments.set(updated);
    this.selectedBlockerIndex.set(null);
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

  landStackTrackKey(item: IndexedPermanent | LandStack): string {
    return isLandStack(item) ? item.lands[0].perm.id : item.perm.id;
  }

  isPermanentCreature(perm: Permanent): boolean {
    return isPermanentCreature(perm);
  }

  myCreaturesNotInCombat = computed(() => {
    const selectedAttackers = this.selectedAttackerIndices();
    const assignedBlockers = this.blockerAssignments();
    return splitBattlefield(this.myBattlefield).creatures.filter(c => {
      if (selectedAttackers.has(c.originalIndex)) return false;
      if (assignedBlockers.has(c.originalIndex)) return false;
      if (c.perm.attacking || c.perm.blocking) return false;
      return true;
    });
  });

  opponentCreaturesNotInCombat = computed(() => {
    return splitBattlefield(this.opponentBattlefield).creatures.filter(c => {
      if (c.perm.attacking || c.perm.blocking) return false;
      return true;
    });
  });

  // ========== Combat zone ==========

  get showCombatZone(): boolean {
    return this.combatPairings.length > 0;
  }

  get combatPairings(): CombatGroup[] {
    const groups: CombatGroup[] = [];

    const selectedAttackers = this.selectedAttackerIndices();
    if (this.declaringAttackers() || selectedAttackers.size > 0) {
      for (const idx of selectedAttackers) {
        groups.push({
          attackerIndex: idx,
          attacker: this.myBattlefield[idx],
          attackerIsMine: true,
          blockers: []
        });
      }
      return groups;
    }

    const myBf = this.myBattlefield;
    const oppBf = this.opponentBattlefield;
    const myAttacking = myBf.some(p => p.attacking);
    const oppAttacking = oppBf.some(p => p.attacking);

    if (myAttacking) {
      myBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: true, blockers: [] };
        oppBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTargets.includes(idx)) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: false });
          }
        });
        groups.push(group);
      });
    } else if (oppAttacking) {
      oppBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: false, blockers: [] };
        myBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTargets.includes(idx)) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: true });
          }
        });
        const assignments = this.blockerAssignments();
        if (this.declaringBlockers() || assignments.size > 0) {
          for (const [blockerIdx, atkIdx] of assignments) {
            if (atkIdx === idx) {
              const alreadyIncluded = group.blockers.some(b => b.index === blockerIdx && b.isMine);
              if (!alreadyIncluded) {
                group.blockers.push({ index: blockerIdx, perm: myBf[blockerIdx], isMine: true });
              }
            }
          }
        }
        groups.push(group);
      });
    }

    return groups;
  }

  // ========== Click dispatch ==========

  private readonly attackingCreatureFilter = (p: Permanent) => isPermanentCreature(p) && p.attacking;

  onMyBattlefieldCardClick(index: number): void {
    const perm = this.myBattlefield[index];
    if (this.clickResolver.tryResolveClick(perm, this.attackingCreatureFilter)) return;
    if (this.choice.targeting.convoking) {
      if (perm && isPermanentCreature(perm) && !perm.tapped) {
        this.choice.targeting.toggleConvokeCreature(perm.id);
      }
      return;
    }
    if (this.choice.targeting.selectingAlternateCostCreatures) {
      if (perm && isPermanentCreature(perm)) {
        this.choice.targeting.toggleAlternateCostCreature(perm.id);
      }
      return;
    }
    if (this.choice.awaitingXValueChoice || (this.choice.awaitingMayAbility && this.choice.mayAbilityManaCost != null)) {
      if (perm && this.choice.canTapForMana(perm)) {
        const manaAbilityIndex = perm.card.activatedAbilities.findIndex(a => a.isManaAbility);
        if (manaAbilityIndex >= 0) {
          this.websocketService.send({ type: MessageType.ACTIVATE_ABILITY, permanentIndex: index, abilityIndex: manaAbilityIndex });
        } else {
          this.websocketService.send({ type: MessageType.TAP_PERMANENT, permanentIndex: index });
        }
      }
      return;
    }
    if (this.declaringAttackers()) {
      this.toggleAttacker(index);
    } else if (this.declaringBlockers()) {
      this.selectBlocker(index);
    } else {
      this.choice.targeting.tapPermanent(index);
    }
  }

  onOpponentBattlefieldCardClick(index: number): void {
    const perm = this.opponentBattlefield[index];
    if (this.clickResolver.tryResolveClick(perm, this.attackingCreatureFilter)) return;
    if (this.declaringBlockers()) {
      this.assignBlock(index);
    }
  }

  onCombatAttackerClick(group: CombatGroup): void {
    if (this.clickResolver.tryResolveClick(group.attacker, () => true)) return;
    if (this.declaringAttackers() && group.attackerIsMine) {
      this.toggleAttacker(group.attackerIndex);
    } else if (this.declaringBlockers() && !group.attackerIsMine) {
      this.assignBlock(group.attackerIndex);
    } else if (group.attackerIsMine) {
      this.choice.targeting.tapPermanent(group.attackerIndex);
    }
  }

  onCombatBlockerClick(blocker: CombatBlocker): void {
    if (this.clickResolver.tryResolveClick(blocker.perm)) return;
    if (this.declaringBlockers() && blocker.isMine) {
      this.selectBlocker(blocker.index);
    } else if (blocker.isMine) {
      this.choice.targeting.tapPermanent(blocker.index);
    }
  }

  // ========== Stack display ==========

  onStackEntryHover(entry: StackEntry): void {
    this.stackTargetId.set(entry.targetPermanentId);
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
    if (!entry.targetPermanentId) return null;
    const g = this.game();
    if (!g) return null;
    const playerIdx = g.playerIds.indexOf(entry.targetPermanentId);
    if (playerIdx >= 0) return g.playerNames[playerIdx];
    for (const bf of g.battlefields) {
      for (const perm of bf) {
        if (perm.id === entry.targetPermanentId) return perm.card.name;
      }
    }
    for (const se of g.stack) {
      if (se.cardId === entry.targetPermanentId) return se.card.name;
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

  onCardHover(card: Card, permanent: Permanent | null = null): void {
    this.hoveredCard.set(card);
    this.hoveredPermanent.set(permanent);
  }

  onCardHoverEnd(): void {
    this.hoveredCard.set(null);
    this.hoveredPermanent.set(null);
  }

  // ========== Formatting ==========

  formatAbilityDescription(description: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(
      this.manaSymbolService.replaceSymbols(description)
    );
  }

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
