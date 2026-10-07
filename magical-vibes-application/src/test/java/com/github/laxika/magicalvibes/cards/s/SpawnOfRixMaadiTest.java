package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawnOfRixMaadi.class, DrudgeBeetle.class})
class SpawnOfRixMaadiTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castSpawnOfRixMaadi(true);

        Permanent spawn = findPermanent(player1, "Spawn of Rix Maadi");
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castSpawnOfRixMaadi(false);

        assertThat(findPermanent(player1, "Spawn of Rix Maadi").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Spawn of Rix Maadi can't block")
    void unleashedCantBlock() {
        Permanent spawn = addCreatureReady(player1, new SpawnOfRixMaadi());
        spawn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new SpawnOfRixMaadi());
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Spawn of Rix Maadi").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction is block-only — an unleashed Spawn of Rix Maadi can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent spawn = addCreatureReady(player1, new SpawnOfRixMaadi());
        spawn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Removing the unleash counter allows it to block again")
    void blocksAfterUnleashCounterIsRemoved() {
        castSpawnOfRixMaadi(true);
        Permanent spawn = findPermanent(player1, "Spawn of Rix Maadi");
        spawn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(spawn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A counter received after declining unleash still prevents blocking")
    void laterCounterPreventsBlocking() {
        castSpawnOfRixMaadi(false);
        Permanent spawn = findPermanent(player1, "Spawn of Rix Maadi");
        spawn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters do not prevent blocking")
    void blocksWithOtherCounterType() {
        Permanent spawn = addCreatureReady(player1, new SpawnOfRixMaadi());
        spawn.setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(spawn.isBlocking()).isTrue();
    }

    private void castSpawnOfRixMaadi(boolean unleash) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SpawnOfRixMaadi(), "{3}{B}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
