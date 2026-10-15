package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SqueeGoblinNabob;
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

@CardUsed({LegolasGreenleaf.class, GrizzlyBears.class, HillGiant.class, SqueeGoblinNabob.class})
class LegolasGreenleafTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent legolas = addCreatureReady(player1, new LegolasGreenleaf());
        legolas.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(legolas);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByHighPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent legolas = addCreatureReady(player1, new LegolasGreenleaf());
        legolas.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(legolas);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when another legendary creature enters under its controller's control")
    void getsCounterWhenLegendaryCreatureEnters() {
        Permanent legolas = harness.enterBattlefieldAndReturn(player1, new LegolasGreenleaf());

        harness.enterBattlefieldAndReturn(player1, new SqueeGoblinNabob());
        harness.passBothPriorities();

        assertThat(legolas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when an opponent's legendary creature enters")
    void noCounterWhenOpponentsLegendaryCreatureEnters() {
        Permanent legolas = harness.enterBattlefieldAndReturn(player1, new LegolasGreenleaf());

        harness.enterBattlefieldAndReturn(player2, new SqueeGoblinNabob());
        harness.passBothPriorities();

        assertThat(legolas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent legolas = addCreatureReady(player1, new LegolasGreenleaf());
        legolas.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void noCounterForOwnEntry() {
        Permanent legolas = harness.enterBattlefieldAndReturn(player1, new LegolasGreenleaf());
        harness.passBothPriorities();

        assertThat(legolas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for another nonlegendary creature")
    void noCounterForNonlegendaryCreature() {
        Permanent legolas = harness.enterBattlefieldAndReturn(player1, new LegolasGreenleaf());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(legolas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Checks a blocker's current power including counters")
    void canBeBlockedByCreatureWhosePowerIncreased() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent legolas = addCreatureReady(player1, new LegolasGreenleaf());
        legolas.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(legolas);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
    @Test
    @DisplayName("Does not draw when combat damage is dealt only to a blocker")
    void noDrawWhenBlocked() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent legolas = addCreatureReady(player1, new LegolasGreenleaf());
        legolas.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(legolas))));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }
}