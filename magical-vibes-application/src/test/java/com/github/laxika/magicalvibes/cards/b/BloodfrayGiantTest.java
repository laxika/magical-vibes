package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ExperimentOne;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodfrayGiant.class, BellowsLizard.class, ExperimentOne.class})
class BloodfrayGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castGiant(true);

        Permanent giant = findPermanent(player1, "Bloodfray Giant");
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castGiant(false);

        assertThat(findPermanent(player1, "Bloodfray Giant")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Bloodfray Giant can't block")
    void unleashedCantBlock() {
        Permanent giant = addCreatureReady(player1, new BloodfrayGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new BellowsLizard());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new BloodfrayGiant());
        addCreatureReady(player2, new BellowsLizard());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Bloodfray Giant").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unleashed Bloodfray Giant can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent giant = addCreatureReady(player1, new BloodfrayGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Unleash counters are included when checking whether evolve triggers")
    void unleashedEntryTriggersEvolveWithGreaterPower() {
        Permanent experiment = addCreatureReady(player1, new ExperimentOne());
        experiment.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castGiant(true);
        resolveAllTriggers();

        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining unleash does not trigger evolve on a 4/4 creature")
    void declinedEntryDoesNotTriggerEvolveAtEqualPower() {
        Permanent experiment = addCreatureReady(player1, new ExperimentOne());
        experiment.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castGiant(false);
        resolveAllTriggers();

        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing the unleash counter allows the Giant to block")
    void blocksAfterUnleashCounterIsRemoved() {
        castGiant(true);
        Permanent giant = findPermanent(player1, "Bloodfray Giant");
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new BellowsLizard());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Other counter types do not prevent blocking")
    void blocksWithQuestCounter() {
        Permanent giant = addCreatureReady(player1, new BloodfrayGiant());
        giant.setCounterCount(CounterType.QUEST, 1);
        addCreatureReady(player2, new BellowsLizard());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unleashed Giant tramples over a blocker")
    void unleashedGiantDealsExcessDamageToDefender() {
        Permanent giant = addCreatureReady(player1, new BloodfrayGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent blocker = addCreatureReady(player2, new BellowsLizard());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Bellows Lizard");
    }

    private void castGiant(boolean unleash) {
        harness.setHand(player1, List.of(new BloodfrayGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
