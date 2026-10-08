package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlchemistsApprentice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanderingWolf.class, AlchemistsApprentice.class})
class WanderingWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by a creature with less power")
    void cannotBeBlockedByLowerPower() {
        Permanent blocker = addCreatureReady(player2, new AlchemistsApprentice());
        Permanent wolf = addCreatureReady(player1, new WanderingWolf());
        wolf.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(wolf);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("Can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new WanderingWolf());
        Permanent wolf = addCreatureReady(player1, new WanderingWolf());
        wolf.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(wolf);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by a creature with greater current power")
    void canBeBlockedByGreaterPower() {
        Permanent blocker = addCreatureReady(player2, new WanderingWolf());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent wolf = addCreatureReady(player1, new WanderingWolf());
        wolf.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Increasing the wolf's power prevents a formerly equal-power creature from blocking")
    void restrictionUsesAttackersCurrentPower() {
        addCreatureReady(player2, new WanderingWolf());
        Permanent wolf = addCreatureReady(player1, new WanderingWolf());
        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        wolf.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("A boosted blocker with equal current power can block")
    void restrictionUsesBlockersCurrentPower() {
        Permanent blocker = addCreatureReady(player2, new AlchemistsApprentice());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent wolf = addCreatureReady(player1, new WanderingWolf());
        wolf.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A wolf that loses its abilities can be blocked by a lower-power creature")
    void losingAbilitiesRemovesRestriction() {
        Permanent blocker = addCreatureReady(player2, new AlchemistsApprentice());
        Permanent wolf = addCreatureReady(player1, new WanderingWolf());
        wolf.setLosesAllAbilitiesUntilEndOfTurn(true);
        wolf.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
