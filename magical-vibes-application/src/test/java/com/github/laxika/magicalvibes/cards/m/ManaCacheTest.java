package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaCache.class, TerrainGenerator.class})
class ManaCacheTest extends BaseCardTest {

    @Test
    @DisplayName("Adds charge counters for the untapped lands of the active player's end step")
    void addsCountersForActivePlayersUntappedLands() {
        Permanent cache = addCache(player1);
        addLand(player1);
        addLand(player1);
        Permanent activePlayersUntappedLand = addLand(player2);
        Permanent activePlayersTappedLand = addLand(player2);
        activePlayersTappedLand.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
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
    @DisplayName("Cannot activate the mana ability without a charge counter")
    void cannotActivateWithoutChargeCounter() {
        Permanent cache = addCache(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(cache.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The mana ability can be activated during postcombat main before the end step")
    void allowsActivationDuringPostcombatMain() {
        Permanent cache = addCache(player1);
        cache.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
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

    private Permanent addLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new TerrainGenerator());
        land.setSummoningSick(false);
        return land;
    }
}
