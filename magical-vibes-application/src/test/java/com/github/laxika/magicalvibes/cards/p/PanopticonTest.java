package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.planar.*;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.planar.*;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@CardUsed(Panopticon.class)
class PanopticonTest extends BaseCardTest {
    private PlanechaseService planar;
    private PlanarDieRoller die;

    @BeforeEach
    void preparePlanes() {
        die = mock(PlanarDieRoller.class);
        planar = new PlanechaseService(die, gqs,
                GameTestEngineContext.get().getBean(GameLogService.class),
                GameTestEngineContext.get().getBean(TriggerCollectionService.class),
                GameTestEngineContext.get().getBean(ConditionEvaluationService.class),
                GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.cards.CardCatalog.class));
        planar.initializeDeck(gd);
        gd.startingPlayerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> planar.start(gd));
    }

    @Test
    void startingPlaneDoesNotDraw() {
        assertThat(gd.stack).isEmpty();
        assertThat(gd.planechase.deck).hasSize(19);
        assertThat(gd.planechase.faceUp).hasSize(1);
        assertThat(gd.planechase.deck.stream().map(Card::getId).distinct()).hasSize(19);
    }

    @Test
    void chaosDrawsForPlanarController() {
        int before = gd.playerHands.get(player1.getId()).size();
        when(die.roll()).thenReturn(PlanarDieResult.CHAOS);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    void planeswalkingUsesStackAndDrawsFromTheNewCopy() {
        var oldId = gd.planechase.faceUp.getFirst().getId();
        int before = gd.playerHands.get(player1.getId()).size();
        when(die.roll()).thenReturn(PlanarDieResult.PLANESWALKER);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.planechase.faceUp.getFirst().getId()).isEqualTo(oldId);
        harness.passBothPriorities();
        assertThat(gd.planechase.faceUp.getFirst().getId()).isNotEqualTo(oldId);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    void blankRollLeavesThePlaneAndStackUnchanged() {
        var old = gd.planechase.faceUp.getFirst();
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.planechase.faceUp).containsExactly(old);
        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player1.getId());
    }

    @Test
    void rollsCostZeroThenOneThenTwoMana() {
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(3);
    }

    @Test
    void failedPaymentChangesNothing() {
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        long sequence = gd.planechase.rollSequence;
        assertThatIllegalStateException().isThrownBy(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.planechase.rollSequence).isEqualTo(sequence);
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(1);
        verify(die, times(1)).roll();
    }

    @Test
    void effectGeneratedRollDoesNotIncreaseSpecialActionCost() {
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(1);
    }

    @Test
    void newTurnResetsTheRollCostIncludingExtraTurns() {
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        gd.turnNumber++;
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(1);
    }

    @Test
    void opponentCannotRollDuringActivePlayersTurn() {
        assertThatIllegalStateException().isThrownBy(() -> gs.rollPlanarDie(gd, player2));
        assertThat(gd.planechase.rollSequence).isZero();
    }

    @Test
    void cannotRollDuringUpkeepOrInResponse() {
        harness.forceStep(TurnStep.UPKEEP);
        assertThatIllegalStateException().isThrownBy(() -> gs.rollPlanarDie(gd, player1));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.inMutationScope(() -> planar.chaos(gd));
        assertThatIllegalStateException().isThrownBy(() -> gs.rollPlanarDie(gd, player1));
    }

    @Test
    void eitherPlayersDrawStepDrawsOneAdditionalCard() {
        int before = gd.playerHands.get(player2.getId()).size();
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(before + 2);
    }

    @Test
    void firstPlayersSkippedDrawStepDoesNotTrigger() {
        int before = gd.playerHands.get(player1.getId()).size();
        gd.turnNumber = 1;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void planarStateCopiesAreIndependent() {
        var copy = gd.planechase.copy();
        copy.deck.clear();
        copy.faceUp.getFirst().getCounters().put(CounterType.CHARGE, 2);
        assertThat(gd.planechase.deck).hasSize(19);
        assertThat(gd.planechase.faceUp.getFirst().getCounters()).isEmpty();
    }

    @Test
    void faceDownPlanesDoNotAddChaosTriggers() {
        harness.inMutationScope(() -> planar.chaos(gd));
        int before = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restartReturnsAllPlanarCardsAndSuppressesStartingTriggers() {
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        harness.inMutationScope(() -> planar.restart(gd));
        assertThat(gd.planechase.deck).hasSize(20);
        harness.inMutationScope(() -> planar.start(gd));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isZero();
    }
    @Test
    void restrictedSpellAndAbilityManaCannotPayForTheSpecialAction() {
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addAbilityOnlyMana(ManaColor.RED, 1);
        pool.addCreatureMana(ManaColor.GREEN, 1);
        assertThatThrownBy(() -> planar.rollSpecialAction(gd, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        pool.addPowerstoneOnlyColorless(1);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(2);
        assertThat(pool.getAbilityOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getCreatureMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @CardUsed(com.github.laxika.magicalvibes.cards.f.FaridehDevilsChosen.class)
    void planarRollTriggersDiceAbilitiesWithoutProducingANumericalResult() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FaridehDevilsChosen());
        int before = gd.playerHands.get(player1.getId()).size();
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);
        harness.inMutationScope(() -> planar.rollSpecialAction(gd, player1.getId()));
        harness.passBothPriorities();
        Permanent farideh = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, farideh, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, farideh, Keyword.MENACE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
    }

    @Test
    void gameActionAcceptsTheAuthenticatedPlayersFreeRoll() {
        gs.rollPlanarDie(gd, player1);
        assertThat(gd.planechase.rollSequence).isEqualTo(1);
        assertThat(gd.planechase.lastRollPlayerId).isEqualTo(player1.getId());
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(1);
    }

    @Test
    void openingPlaneIsRevealedOnlyAfterBothPlayersKeepTheirHands() {
        GameTestHarness opening = new GameTestHarness();
        GameData startingGame = opening.getGameData();
        var service = GameTestEngineContext.get().getBean(PlanechaseService.class);
        service.initializeDeck(startingGame);
        assertThat(startingGame.planechase.faceUp).isEmpty();
        opening.getGameService().keepHand(startingGame, opening.getPlayer1());
        assertThat(startingGame.planechase.faceUp).isEmpty();
        opening.getGameService().keepHand(startingGame, opening.getPlayer2());
        assertThat(startingGame.status).isEqualTo(GameStatus.RUNNING);
        assertThat(startingGame.planechase.faceUp).hasSize(1);
        assertThat(startingGame.stack).isEmpty();
        assertThat(startingGame.playerHands.get(opening.getPlayer1().getId())).hasSize(7);
    }

}
