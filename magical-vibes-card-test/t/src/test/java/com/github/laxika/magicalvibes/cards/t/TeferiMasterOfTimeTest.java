package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferiMasterOfTime.class, AlpineWatchdog.class, Shock.class})
class TeferiMasterOfTimeTest extends BaseCardTest {

    @Test
    void plusOneDrawsThenDiscards() {
        Permanent teferi = addReadyTeferi(player1, 3);
        Shock keptInHand = new Shock();
        harness.setHand(player1, List.of(keptInHand));
        AlpineWatchdog drawn = new AlpineWatchdog();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(keptInHand);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusThreePhasesOutCreatureAnOpponentControls() {
        Permanent teferi = addReadyTeferi(player1, 3);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        harness.activateAbility(player1, 0, 1, null, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, opposingCreature.getId())).isNull();
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opposingCreature.getId()));
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void minusThreeCannotTargetYourCreature() {
        addReadyTeferi(player1, 3);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void loyaltyAbilitiesCanBeActivatedOnAnOpponentsTurnWithAStackPresent() {
        Permanent teferi = addReadyTeferi(player1, 3);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, opposingCreature.getId())).isNull();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void minusTenTakesTwoExtraTurns() {
        Permanent teferi = addReadyTeferi(player1, 10);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(teferi);
        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        addReadyTeferi(player1, 3);
        Shock originalCard = new Shock();
        AlpineWatchdog drawn = new AlpineWatchdog();
        harness.setHand(player1, List.of(originalCard));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void cannotActivateAnotherLoyaltyAbilityBeforeTheFirstResolves() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one loyalty ability");
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canActivateOnTheNextOpponentsTurnAfterActivatingOnYourTurn() {
        Permanent teferi = addReadyTeferi(player1, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void canActivateDuringAnOpponentsUpkeep() {
        Permanent teferi = addReadyTeferi(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void phasedOutCreatureReturnsOnlyAtItsControllersUntapWithCountersIntact() {
        addReadyTeferi(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        harness.performUntapStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).doesNotContain(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void minusThreeCannotTargetANoncreature() {
        addReadyTeferi(player1, 4);
        Permanent opposingPlaneswalker = harness.addToBattlefieldAndReturn(player2, new TeferiMasterOfTime());
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opposingPlaneswalker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayMinusThreeWithInsufficientLoyalty() {
        Permanent teferi = addReadyTeferi(player1, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty counters");
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayMinusTenWithInsufficientLoyalty() {
        Permanent teferi = addReadyTeferi(player1, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty counters");
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(9);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void extraTurnsOccurBeforeTheOpponentsNextTurn() {
        addReadyTeferi(player1, 10);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(null, TurnStep.UPKEEP);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(null, TurnStep.UPKEEP);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(null, TurnStep.UPKEEP);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    private Permanent addReadyTeferi(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TeferiMasterOfTime());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
