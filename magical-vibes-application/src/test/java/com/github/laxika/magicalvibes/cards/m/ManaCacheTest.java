package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManaCacheTest extends BaseCardTest {

    @Test
    @DisplayName("Adds charge counters for the untapped lands of the active player's end step")
    void addsCountersForActivePlayersUntappedLands() {
        Permanent cache = addCache(player1);
        addForest(player1);
        addForest(player1);
        Permanent activePlayersUntappedLand = addForest(player2);
        Permanent activePlayersTappedLand = addForest(player2);
        activePlayersTappedLand.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cache.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(activePlayersUntappedLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Any player may remove a charge counter for colorless mana during their own turn")
    void anyPlayerMayActivateForColorlessMana() {
        Permanent cache = addCache(player1);
        cache.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, null);

        assertThat(cache.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability cannot be activated outside the activating player's turn before the end step")
    void enforcesActivationTiming() {
        Permanent cache = addCache(player1);
        cache.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before the end step");
    }

    private Permanent addCache(Player player) {
        Permanent cache = harness.addToBattlefieldAndReturn(player, new ManaCache());
        cache.setSummoningSick(false);
        return cache;
    }

    private Permanent addForest(Player player) {
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        forest.setSummoningSick(false);
        return forest;
    }
}
