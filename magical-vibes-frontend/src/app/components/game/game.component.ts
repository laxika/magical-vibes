import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { WebsocketService, Game, GameNotification, GameUpdate, GameStatus, MessageType, TurnStep, PHASE_GROUPS, Card, Permanent, HandDrawnNotification, MulliganResolvedNotification, GameStartedNotification, SelectCardsToBottomNotification, DeckSizesUpdatedNotification, PlayableCardsNotification, BattlefieldUpdatedNotification, ManaUpdatedNotification, AutoStopsUpdatedNotification, AvailableAttackersNotification, AvailableBlockersNotification, LifeUpdatedNotification, GameOverNotification, ChooseCardFromHandNotification } from '../../services/websocket.service';
import { Subscription } from 'rxjs';

export interface IndexedPermanent {
  perm: Permanent;
  originalIndex: number;
}

export interface CombatBlocker {
  index: number;
  perm: Permanent;
  isMine: boolean;
}

export interface CombatGroup {
  attackerIndex: number;
  attacker: Permanent;
  attackerIsMine: boolean;
  blockers: CombatBlocker[];
}

@Component({
  selector: 'app-game',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './game.component.html',
  styleUrl: './game.component.css'
})
export class GameComponent implements OnInit, OnDestroy {
  game = signal<Game | null>(null);
  private subscriptions: Subscription[] = [];

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

    const initialStops = this.websocketService.currentGame?.autoStopSteps;
    if (initialStops) {
      this.autoStopSteps.set(new Set(initialStops));
    }

    this.subscriptions.push(
      this.websocketService.getMessages().subscribe((message) => {
        const notification = message as GameNotification;

        console.log(notification);

        if (notification.type === MessageType.OPPONENT_JOINED && notification.game) {
          this.game.set(notification.game);
          this.websocketService.currentGame = notification.game;
        }

        if (message.type === MessageType.GAME_LOG_ENTRY) {
          this.appendLogEntry((message as GameNotification).message!);
        }

        if (message.type === MessageType.HAND_DRAWN) {
          const handDrawn = message as HandDrawnNotification;
          this.updateHand(handDrawn.hand, handDrawn.mulliganCount);
        }

        if (message.type === MessageType.MULLIGAN_RESOLVED) {
          const resolved = message as MulliganResolvedNotification;
          this.handleMulliganResolved(resolved);
        }

        if (message.type === MessageType.GAME_STARTED) {
          const started = message as GameStartedNotification;
          this.handleGameStarted(started);
        }

        if (message.type === MessageType.SELECT_CARDS_TO_BOTTOM) {
          const selectMsg = message as SelectCardsToBottomNotification;
          this.handleSelectCardsToBottom(selectMsg);
        }

        if (message.type === MessageType.DECK_SIZES_UPDATED) {
          const deckMsg = message as DeckSizesUpdatedNotification;
          this.updateDeckSizes(deckMsg.deckSizes);
        }

        if (message.type === MessageType.PLAYABLE_CARDS_UPDATED) {
          const playableMsg = message as PlayableCardsNotification;
          this.playableCardIndices.set(new Set(playableMsg.playableCardIndices));
        }

        if (message.type === MessageType.BATTLEFIELD_UPDATED) {
          const bfMsg = message as BattlefieldUpdatedNotification;
          this.updateBattlefields(bfMsg.battlefields);
        }

        if (message.type === MessageType.MANA_UPDATED) {
          const manaMsg = message as ManaUpdatedNotification;
          this.updateManaPool(manaMsg.manaPool);
        }

        if (message.type === MessageType.AUTO_STOPS_UPDATED) {
          const stopsMsg = message as AutoStopsUpdatedNotification;
          this.autoStopSteps.set(new Set(stopsMsg.autoStopSteps));
        }

        if (message.type === MessageType.AVAILABLE_ATTACKERS) {
          const atkMsg = message as AvailableAttackersNotification;
          this.handleAvailableAttackers(atkMsg);
        }

        if (message.type === MessageType.AVAILABLE_BLOCKERS) {
          const blkMsg = message as AvailableBlockersNotification;
          this.handleAvailableBlockers(blkMsg);
        }

        if (message.type === MessageType.LIFE_UPDATED) {
          const lifeMsg = message as LifeUpdatedNotification;
          this.updateLifeTotals(lifeMsg.lifeTotals);
        }

        if (message.type === MessageType.GAME_OVER) {
          const goMsg = message as GameOverNotification;
          this.handleGameOver(goMsg);
        }

        if (message.type === MessageType.CHOOSE_CARD_FROM_HAND) {
          const chooseMsg = message as ChooseCardFromHandNotification;
          this.handleChooseCardFromHand(chooseMsg);
        }

        const update = message as GameUpdate;
        if (update.type === MessageType.PRIORITY_UPDATED ||
            update.type === MessageType.STEP_ADVANCED ||
            update.type === MessageType.TURN_CHANGED) {
          this.applyGameUpdate(update);
        }
      })
    );

    this.subscriptions.push(
      this.websocketService.onDisconnected().subscribe(() => {
        this.router.navigate(['/']);
      })
    );
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

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

  private applyGameUpdate(update: GameUpdate): void {
    const g = this.game();
    if (!g) return;

    // Clear playable highlights immediately on any state change;
    // the server's PLAYABLE_CARDS_UPDATED message will re-set them if appropriate
    this.playableCardIndices.set(new Set<number>());

    const updated = { ...g };
    if (update.priorityPlayerId !== undefined) {
      updated.priorityPlayerId = update.priorityPlayerId ?? null;
    }
    if (update.currentStep !== undefined) {
      updated.currentStep = update.currentStep;
    }
    if (update.activePlayerId !== undefined) {
      updated.activePlayerId = update.activePlayerId;
    }
    if (update.turnNumber !== undefined) {
      updated.turnNumber = update.turnNumber;
    }
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private appendLogEntry(entry: string): void {
    const g = this.game();
    if (!g) return;

    const updated = { ...g, gameLog: [...g.gameLog, entry] };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  get isMulliganPhase(): boolean {
    const g = this.game();
    return g !== null && g.status === GameStatus.MULLIGAN;
  }

  get canMulligan(): boolean {
    const g = this.game();
    return g !== null && g.mulliganCount < 7;
  }

  get hand(): Card[] {
    return this.game()?.hand ?? [];
  }

  private updateHand(hand: Card[], mulliganCount: number): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, hand, mulliganCount };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleMulliganResolved(resolved: MulliganResolvedNotification): void {
    const g = this.game();
    if (!g) return;

    const myName = this.websocketService.currentUser?.username;
    if (resolved.playerName === myName) {
      if (resolved.kept) {
        this.selfKept = true;
      }
    } else {
      if (resolved.kept) {
        this.opponentKept = true;
      } else {
        this.opponentKept = false;
      }
    }
  }

  private handleGameStarted(started: GameStartedNotification): void {
    const g = this.game();
    if (!g) return;
    const updated = {
      ...g,
      status: GameStatus.RUNNING,
      activePlayerId: started.activePlayerId,
      turnNumber: started.turnNumber,
      currentStep: started.currentStep,
      priorityPlayerId: started.priorityPlayerId
    };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
    this.opponentKept = false;
    this.selfKept = false;
    this.selectingBottomCards = false;
    this.bottomCardCount = 0;
    this.selectedCardIndices.clear();
  }

  private updateDeckSizes(deckSizes: number[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, deckSizes };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateBattlefields(battlefields: Permanent[][]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, battlefields };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateManaPool(manaPool: Record<string, number>): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, manaPool };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private updateLifeTotals(lifeTotals: number[]): void {
    const g = this.game();
    if (!g) return;
    const updated = { ...g, lifeTotals };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleAvailableAttackers(msg: AvailableAttackersNotification): void {
    this.declaringAttackers = true;
    this.availableAttackerIndices.set(new Set(msg.attackerIndices));
    this.selectedAttackerIndices.clear();
  }

  private handleAvailableBlockers(msg: AvailableBlockersNotification): void {
    this.declaringBlockers = true;
    this.availableBlockerIndices.set(new Set(msg.blockerIndices));
    this.opponentAttackerIndices = msg.attackerIndices;
    this.blockerAssignments.clear();
    this.selectedBlockerIndex = null;
  }

  private handleGameOver(msg: GameOverNotification): void {
    this.gameOverWinner = msg.winnerName;
    this.gameOverWinnerId = msg.winnerId;
    const g = this.game();
    if (!g) return;
    const updated = { ...g, status: GameStatus.FINISHED };
    this.game.set(updated);
    this.websocketService.currentGame = updated;
  }

  private handleChooseCardFromHand(msg: ChooseCardFromHandNotification): void {
    this.choosingFromHand = true;
    this.choosableHandIndices.set(new Set(msg.cardIndices));
    this.handChoicePrompt = msg.prompt;
  }

  chooseCardFromHand(index: number): void {
    const g = this.game();
    if (!g || !this.choosingFromHand) return;
    if (!this.choosableHandIndices().has(index)) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      gameId: g.id,
      cardIndex: index
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  declineHandChoice(): void {
    const g = this.game();
    if (!g || !this.choosingFromHand) return;
    this.websocketService.send({
      type: MessageType.CARD_CHOSEN,
      gameId: g.id,
      cardIndex: -1
    });
    this.choosingFromHand = false;
    this.choosableHandIndices.set(new Set());
    this.handChoicePrompt = '';
  }

  isChoosableCard(index: number): boolean {
    return this.choosableHandIndices().has(index);
  }

  get myPlayerIndex(): number {
    const g = this.game();
    if (!g) return 0;
    return g.playerIds.indexOf(this.websocketService.currentUser?.userId ?? -1);
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

  playCard(index: number): void {
    const g = this.game();
    if (g && this.isCardPlayable(index)) {
      this.websocketService.send({ type: MessageType.PLAY_CARD, gameId: g.id, cardIndex: index });
    }
  }

  get player1DeckSize(): number {
    return this.game()?.deckSizes?.[0] ?? 0;
  }

  get player2DeckSize(): number {
    return this.game()?.deckSizes?.[1] ?? 0;
  }

  private handleSelectCardsToBottom(msg: SelectCardsToBottomNotification): void {
    this.selectingBottomCards = true;
    this.bottomCardCount = msg.count;
    this.selectedCardIndices.clear();
  }

  keepHand(): void {
    const g = this.game();
    if (g && !this.selfKept) {
      this.websocketService.send({ type: MessageType.KEEP_HAND, gameId: g.id });
    }
  }

  takeMulligan(): void {
    const g = this.game();
    if (g && !this.selfKept) {
      this.websocketService.send({ type: MessageType.TAKE_MULLIGAN, gameId: g.id });
    }
  }

  toggleCardSelection(index: number): void {
    if (this.selectedCardIndices.has(index)) {
      this.selectedCardIndices.delete(index);
    } else if (this.selectedCardIndices.size < this.bottomCardCount) {
      this.selectedCardIndices.add(index);
    }
  }

  isCardSelected(index: number): boolean {
    return this.selectedCardIndices.has(index);
  }

  get canConfirmBottom(): boolean {
    return this.selectedCardIndices.size === this.bottomCardCount;
  }

  confirmBottomCards(): void {
    const g = this.game();
    if (g && this.canConfirmBottom) {
      this.websocketService.send({
        type: MessageType.BOTTOM_CARDS,
        gameId: g.id,
        cardIndices: Array.from(this.selectedCardIndices)
      });
      this.selectingBottomCards = false;
      this.selectedCardIndices.clear();
    }
  }

  passPriority(): void {
    const g = this.game();
    if (g) {
      this.websocketService.send({ type: MessageType.PASS_PRIORITY, gameId: g.id });
    }
  }

  tapPermanent(index: number): void {
    const g = this.game();
    if (g && this.canTapPermanent(index)) {
      this.websocketService.send({ type: MessageType.TAP_PERMANENT, gameId: g.id, permanentIndex: index });
    }
  }

  canTapPermanent(index: number): boolean {
    const perm = this.myBattlefield[index];
    if (perm == null || perm.tapped || !this.hasPriority) return false;
    if (perm.card.onTapEffects == null || perm.card.onTapEffects.length === 0) return false;
    if (perm.summoningSick && perm.card.type === 'Creature') return false;
    return true;
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

  opponentKept = false;
  selfKept = false;
  selectingBottomCards = false;
  bottomCardCount = 0;
  selectedCardIndices = new Set<number>();
  playableCardIndices = signal(new Set<number>());
  autoStopSteps = signal(new Set<string>());

  // Combat state
  declaringAttackers = false;
  declaringBlockers = false;
  availableAttackerIndices = signal(new Set<number>());
  availableBlockerIndices = signal(new Set<number>());
  selectedAttackerIndices = new Set<number>();
  opponentAttackerIndices: number[] = [];
  blockerAssignments: Map<number, number> = new Map();
  selectedBlockerIndex: number | null = null;
  gameOverWinner: string | null = null;
  gameOverWinnerId: number | null = null;
  choosingFromHand = false;
  choosableHandIndices = signal(new Set<number>());
  handChoicePrompt = '';

  isCardPlayable(index: number): boolean {
    return this.playableCardIndices().has(index);
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
      gameId: g.id,
      stops: Array.from(current)
    });
  }

  isAutoStop(step: string): boolean {
    return this.autoStopSteps().has(step);
  }

  isForceStop(step: string): boolean {
    return step === TurnStep.PRECOMBAT_MAIN || step === TurnStep.POSTCOMBAT_MAIN;
  }

  // Combat action methods

  canAttack(index: number): boolean {
    return this.declaringAttackers && this.availableAttackerIndices().has(index);
  }

  isSelectedAttacker(index: number): boolean {
    return this.selectedAttackerIndices.has(index);
  }

  toggleAttacker(index: number): void {
    if (!this.canAttack(index)) return;
    if (this.selectedAttackerIndices.has(index)) {
      this.selectedAttackerIndices.delete(index);
    } else {
      this.selectedAttackerIndices.add(index);
    }
  }

  confirmAttackers(): void {
    const g = this.game();
    if (!g) return;
    this.websocketService.send({
      type: MessageType.DECLARE_ATTACKERS,
      gameId: g.id,
      attackerIndices: Array.from(this.selectedAttackerIndices)
    });
    this.declaringAttackers = false;
    this.selectedAttackerIndices.clear();
    this.availableAttackerIndices.set(new Set());
  }

  canBlock(index: number): boolean {
    return this.declaringBlockers && this.availableBlockerIndices().has(index);
  }

  isAssignedBlocker(index: number): boolean {
    return this.blockerAssignments.has(index);
  }

  selectBlocker(index: number): void {
    if (!this.canBlock(index)) return;
    if (this.blockerAssignments.has(index)) {
      this.blockerAssignments.delete(index);
      return;
    }
    this.selectedBlockerIndex = index;
  }

  isBlockTarget(index: number): boolean {
    return this.declaringBlockers && this.selectedBlockerIndex !== null
      && this.opponentBattlefield[index]?.attacking === true;
  }

  assignBlock(attackerIndex: number): void {
    if (this.selectedBlockerIndex === null || !this.declaringBlockers) return;
    const perm = this.opponentBattlefield[attackerIndex];
    if (!perm || !perm.attacking) return;
    this.blockerAssignments.set(this.selectedBlockerIndex, attackerIndex);
    this.selectedBlockerIndex = null;
  }

  confirmBlockers(): void {
    const g = this.game();
    if (!g) return;
    const assignments = Array.from(this.blockerAssignments.entries()).map(([blockerIndex, attackerIndex]) => ({
      blockerIndex,
      attackerIndex
    }));
    this.websocketService.send({
      type: MessageType.DECLARE_BLOCKERS,
      gameId: g.id,
      blockerAssignments: assignments
    });
    this.declaringBlockers = false;
    this.blockerAssignments.clear();
    this.selectedBlockerIndex = null;
    this.availableBlockerIndices.set(new Set());
    this.opponentAttackerIndices = [];
  }

  cancelBlockerSelection(): void {
    this.selectedBlockerIndex = null;
  }

  get myLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.myPlayerIndex] ?? 20;
  }

  get opponentLifeTotal(): number {
    return this.game()?.lifeTotals?.[this.opponentPlayerIndex] ?? 20;
  }

  getLifeTotal(playerIndex: number): number {
    return this.game()?.lifeTotals?.[playerIndex] ?? 20;
  }

  onMyBattlefieldCardClick(index: number): void {
    if (this.declaringAttackers) {
      this.toggleAttacker(index);
    } else if (this.declaringBlockers) {
      this.selectBlocker(index);
    } else {
      this.tapPermanent(index);
    }
  }

  onOpponentBattlefieldCardClick(index: number): void {
    if (this.declaringBlockers) {
      this.assignBlock(index);
    }
  }

  backToLobby(): void {
    this.router.navigate(['/home']);
  }

  // Battlefield splitting: lands (back row) vs creatures (front row)

  private splitBattlefield(battlefield: Permanent[]): { lands: IndexedPermanent[], creatures: IndexedPermanent[] } {
    const lands: IndexedPermanent[] = [];
    const creatures: IndexedPermanent[] = [];
    battlefield.forEach((perm, idx) => {
      const entry: IndexedPermanent = { perm, originalIndex: idx };
      if (perm.card.type === 'Creature') {
        creatures.push(entry);
      } else {
        lands.push(entry);
      }
    });
    return { lands, creatures };
  }

  get myLands(): IndexedPermanent[] {
    return this.splitBattlefield(this.myBattlefield).lands;
  }

  get opponentLands(): IndexedPermanent[] {
    return this.splitBattlefield(this.opponentBattlefield).lands;
  }

  get myCreaturesNotInCombat(): IndexedPermanent[] {
    const inCombat = this.myIndicesInCombat;
    return this.splitBattlefield(this.myBattlefield).creatures.filter(c => !inCombat.has(c.originalIndex));
  }

  get opponentCreaturesNotInCombat(): IndexedPermanent[] {
    const inCombat = this.opponentIndicesInCombat;
    return this.splitBattlefield(this.opponentBattlefield).creatures.filter(c => !inCombat.has(c.originalIndex));
  }

  // Combat zone: groups of attacker + blockers

  get showCombatZone(): boolean {
    return this.combatPairings.length > 0;
  }

  get combatPairings(): CombatGroup[] {
    const groups: CombatGroup[] = [];

    // During declaring attackers: selected attackers (always mine) move to combat zone
    if (this.declaringAttackers) {
      for (const idx of this.selectedAttackerIndices) {
        groups.push({
          attackerIndex: idx,
          attacker: this.myBattlefield[idx],
          attackerIsMine: true,
          blockers: []
        });
      }
      return groups;
    }

    // Server-confirmed attacking creatures
    const myBf = this.myBattlefield;
    const oppBf = this.opponentBattlefield;
    const myAttacking = myBf.some(p => p.attacking);
    const oppAttacking = oppBf.some(p => p.attacking);

    if (myAttacking) {
      // I'm the attacker
      myBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: true, blockers: [] };
        // Opponent's blockers targeting this attacker
        oppBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTarget === idx) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: false });
          }
        });
        groups.push(group);
      });
    } else if (oppAttacking) {
      // Opponent is the attacker
      oppBf.forEach((perm, idx) => {
        if (!perm.attacking) return;
        const group: CombatGroup = { attackerIndex: idx, attacker: perm, attackerIsMine: false, blockers: [] };
        // My server-confirmed blockers
        myBf.forEach((defPerm, defIdx) => {
          if (defPerm.blocking && defPerm.blockingTarget === idx) {
            group.blockers.push({ index: defIdx, perm: defPerm, isMine: true });
          }
        });
        // My locally assigned blockers (during declaration, before server confirms)
        if (this.declaringBlockers) {
          for (const [blockerIdx, atkIdx] of this.blockerAssignments) {
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

  private get myIndicesInCombat(): Set<number> {
    const set = new Set<number>();
    for (const group of this.combatPairings) {
      if (group.attackerIsMine) set.add(group.attackerIndex);
      for (const b of group.blockers) {
        if (b.isMine) set.add(b.index);
      }
    }
    return set;
  }

  private get opponentIndicesInCombat(): Set<number> {
    const set = new Set<number>();
    for (const group of this.combatPairings) {
      if (!group.attackerIsMine) set.add(group.attackerIndex);
      for (const b of group.blockers) {
        if (!b.isMine) set.add(b.index);
      }
    }
    return set;
  }

  onCombatAttackerClick(group: CombatGroup): void {
    if (this.declaringAttackers && group.attackerIsMine) {
      this.toggleAttacker(group.attackerIndex);
    } else if (this.declaringBlockers && !group.attackerIsMine) {
      this.assignBlock(group.attackerIndex);
    }
  }

  onCombatBlockerClick(blocker: CombatBlocker): void {
    if (this.declaringBlockers && blocker.isMine) {
      this.selectBlocker(blocker.index);
    }
  }

  formatKeywords(keywords: string[]): string {
    return keywords.map(k => k.charAt(0) + k.slice(1).toLowerCase()).join(', ');
  }

  readonly GameStatus = GameStatus;
  readonly TurnStep = TurnStep;
  readonly phaseGroups = PHASE_GROUPS;
}
