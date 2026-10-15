package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.cards.a.AetherHerder;
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

@CardUsed({OutlandBoar.class, AetherChaser.class, AetherHerder.class})
class OutlandBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new AetherChaser());
        Permanent boar = addCreatureReady(player1, new OutlandBoar());

        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boar);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent blocker = addCreatureReady(player2, new AetherHerder());
        Permanent boar = addCreatureReady(player1, new OutlandBoar());

        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boar);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A printed 2-power creature can block after a +1/+1 counter raises its power to 3")
    void boostedSmallCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new AetherChaser());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new OutlandBoar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A printed 3-power creature can't block after its power is reduced to 2")
    void reducedLargeCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new AetherHerder());
        blocker.setPowerModifier(-1);
        addCreatureReady(player1, new OutlandBoar());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures with negative power can't block Outland Boar")
    void negativePowerCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new AetherHerder());
        blocker.setPowerModifier(-4);
        addCreatureReady(player1, new OutlandBoar());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing all abilities allows a 2-power creature to block Outland Boar")
    void losingAbilityRemovesBlockingRestriction() {
        Permanent blocker = addCreatureReady(player2, new AetherChaser());
        Permanent boar = addCreatureReady(player1, new OutlandBoar());
        boar.setLosesAllAbilitiesUntilEndOfTurn(true);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
