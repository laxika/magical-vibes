package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NighthowlPursuer.class, CrawWurm.class, RenegadeFreighter.class})
class NighthowlPursuerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a ferocious boost without a creature with power 4 or greater")
    void doesNotBoostWithoutFerocious() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when it attacks while its controller has a creature with power 4 or greater")
    void boostsWithFerocious() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ferocious boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }

    @Test
    void boostStillResolvesAfterQualifyingCreatureLeaves() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        Permanent support = addCreatureReady(player1, new NighthowlPursuer());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(support);
        gd.playerGraveyards.get(player1.getId()).add(support.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(3);
    }

    @Test
    void boostStillResolvesAfterQualifyingCreatureLosesPower() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        Permanent support = addCreatureReady(player1, new NighthowlPursuer());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerWithOnlyThreePowerThenGainingPower() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        Permanent support = addCreatureReady(player1, new NighthowlPursuer());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }

    @Test
    void pursuerCanItselfSatisfyFerociousAtExactlyFourPower() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        pursuer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(6);
    }

    @Test
    void opponentsFourPowerCreatureDoesNotSatisfyFerocious() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        Permanent opponent = addCreatureReady(player2, new NighthowlPursuer());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }

    @Test
    void uncrewedVehicleDoesNotSatisfyFerocious() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        harness.addToBattlefield(player1, new RenegadeFreighter());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }

    @Test
    void menaceRejectsOneBlockerAndAllowsTwo() {
        addCreatureReady(player1, new NighthowlPursuer());
        addCreatureReady(player2, new NighthowlPursuer());
        addCreatureReady(player2, new NighthowlPursuer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}
