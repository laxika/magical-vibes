package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparringGolem.class, YavimayaBarbarian.class, ScorchingLava.class})
class SparringGolemTest extends BaseCardTest {

    @Test
    @DisplayName("With one blocker Sparring Golem gets +1/+1 until end of turn")
    void oneBlockerGivesPlusOne() {
        Permanent golem = addCreatureReady(player1, new SparringGolem());
        golem.setAttacking(true);
        addCreatureReady(player2, new YavimayaBarbarian());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(golem.getPowerModifier()).isEqualTo(1);
        assertThat(golem.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With three blockers Sparring Golem gets +3/+3 until end of turn")
    void threeBlockersGivesPlusThree() {
        Permanent golem = addCreatureReady(player1, new SparringGolem());
        golem.setAttacking(true);
        addCreatureReady(player2, new YavimayaBarbarian());
        addCreatureReady(player2, new YavimayaBarbarian());
        addCreatureReady(player2, new YavimayaBarbarian());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)
        ));
        harness.passBothPriorities();

        assertThat(golem.getPowerModifier()).isEqualTo(3);
        assertThat(golem.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("If unblocked Sparring Golem gets no bonus")
    void unblockedGetsNoBonus() {
        Permanent golem = addCreatureReady(player1, new SparringGolem());
        golem.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(golem.getPowerModifier()).isZero();
        assertThat(golem.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The blocking bonus wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent golem = addCreatureReady(player1, new SparringGolem());
        golem.setAttacking(true);
        addCreatureReady(player2, new YavimayaBarbarian());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(golem.getPowerModifier()).isEqualTo(1);
        assertThat(golem.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(golem.getPowerModifier()).isZero();
        assertThat(golem.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only creatures still blocking at resolution contribute to the bonus")
    void removedBlockerDoesNotContributeToBonus() {
        Permanent golem = addCreatureReady(player1, new SparringGolem());
        golem.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new YavimayaBarbarian());
        addCreatureReady(player2, new YavimayaBarbarian());
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, blocker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        resolveAllTriggers();

        assertThat(golem.getPowerModifier()).isEqualTo(1);
        assertThat(golem.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Sparring Golem counts only its own blockers")
    void eachGolemCountsOnlyItsOwnBlockers() {
        Permanent first = addCreatureReady(player1, new SparringGolem());
        Permanent second = addCreatureReady(player1, new SparringGolem());
        first.setAttacking(true);
        second.setAttacking(true);
        addCreatureReady(player2, new YavimayaBarbarian());
        addCreatureReady(player2, new YavimayaBarbarian());
        addCreatureReady(player2, new YavimayaBarbarian());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1),
                new BlockerAssignment(2, 1)));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
    }
}
