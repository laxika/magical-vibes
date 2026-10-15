package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverlordOfTheBoilerbilges.class})
class OverlordOfTheBoilerbilgesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield deals 4 damage to a target player")
    void enteringDealsDamageToPlayer() {
        castNormally();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Attacking deals 4 damage to a target player")
    void attackingDealsDamageToPlayer() {
        Permanent overlord = addCreatureReady(player1, new OverlordOfTheBoilerbilges());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 11);
        assertThat(overlord.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Casting with impending enters with four time counters and is not a creature")
    void impendingCastEntersWithCountersAndIsNotCreature() {
        Permanent overlord = castWithImpending();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    @DisplayName("Removing the last impending counter makes it a creature")
    void lastCounterMakesItCreature() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    void enteringCanDealDamageToCreature() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());
        castNormally();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    void impendingStillDealsEnterDamage() {
        castWithImpending();

        harness.assertLife(player2, 16);
    }

    @Test
    void enterDamageResolvesAfterSourceLeavesBattlefield() {
        castNormally();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent overlord = findPermanent(player1, "Overlord of the Boilerbilges");
        gd.playerBattlefields.get(player1.getId()).remove(overlord);
        gd.playerGraveyards.get(player1.getId()).add(overlord.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void addingTimeCounterAfterImpendingExpiresMakesItNoncreatureAgain() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);
        advanceToOwnEndStep();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();

        overlord.setCounterCount(CounterType.TIME, 1);

        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void ownEndStepRemovesExactlyOneCounter() {
        Permanent overlord = castWithImpending();

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void opponentsEndStepDoesNotRemoveCounter() {
        Permanent overlord = castWithImpending();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void normalCastIsCreatureAndDoesNotRemoveAddedTimeCounter() {
        castNormally();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        Permanent overlord = findPermanent(player1, "Overlord of the Boilerbilges");
        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    private void castNormally() {
        harness.castFromHand(player1, new OverlordOfTheBoilerbilges(), "{4}{R}{R}");
        harness.passBothPriorities();
    }

    private Permanent castWithImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheBoilerbilges()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        return findPermanent(player1, "Overlord of the Boilerbilges");
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
