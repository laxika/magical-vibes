package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustvineCultivator.class, Forest.class})
class RustvineCultivatorTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps the creature and puts an oil counter on it")
    void putsOilCounterOnSelf() {
        Permanent cultivator = addReadyCultivator(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(cultivator.isTapped()).isTrue();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability removes an oil counter and untaps target land")
    void removesOilCounterAndUntapsTargetLand() {
        Permanent cultivator = addReadyCultivator(player1, 1);
        Permanent forest = addTappedForest(player2);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(cultivator.isTapped()).isTrue();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isZero();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability requires an oil counter")
    void cannotActivateWithoutOilCounter() {
        Permanent cultivator = addReadyCultivator(player1, 0);
        Permanent forest = addTappedForest(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cultivator.isTapped()).isFalse();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Second ability cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent cultivator = addReadyCultivator(player1, 1);
        Permanent nonland = addCreatureReady(player2, new RustvineCultivator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonland.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cultivator.isTapped()).isFalse();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Oil is added on resolution, not as an activation cost")
    void oilCounterIsAddedOnlyOnResolution() {
        Permanent cultivator = addReadyCultivator(player1, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cultivator.isTapped()).isTrue();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exactly one oil counter is paid before the land untaps")
    void paysOilCounterBeforeResolution() {
        Permanent cultivator = addReadyCultivator(player1, 3);
        Permanent forest = addTappedForest(player1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());

        assertThat(cultivator.isTapped()).isTrue();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both tap abilities are unavailable with summoning sickness")
    void cannotActivateWhileSummoningSick() {
        Permanent cultivator = addReadyCultivator(player1, 1);
        cultivator.setSummoningSick(true);
        Permanent forest = addTappedForest(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cultivator.isTapped()).isFalse();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land still untaps if the Cultivator leaves after activation")
    void resolvesIndependentlyOfSource() {
        Permanent cultivator = addReadyCultivator(player1, 1);
        Permanent forest = addTappedForest(player1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(cultivator);
        gd.playerGraveyards.get(player1.getId()).add(cultivator.getCard());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already untapped land is a legal target")
    void canTargetUntappedLand() {
        Permanent cultivator = addReadyCultivator(player1, 1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(cultivator.isTapped()).isTrue();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isZero();
        assertThat(forest.isTapped()).isFalse();
    }

    private Permanent addReadyCultivator(Player player, int oilCounters) {
        Permanent cultivator = addCreatureReady(player, new RustvineCultivator());
        cultivator.setCounterCount(CounterType.OIL, oilCounters);
        return cultivator;
    }

    private Permanent addTappedForest(Player player) {
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        forest.tap();
        return forest;
    }
}
