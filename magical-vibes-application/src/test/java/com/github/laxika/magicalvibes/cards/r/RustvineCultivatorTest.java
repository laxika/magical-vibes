package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cultivator.isTapped()).isFalse();
        assertThat(cultivator.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    private Permanent addReadyCultivator(Player player, int oilCounters) {
        Permanent cultivator = new Permanent(new RustvineCultivator());
        cultivator.setSummoningSick(false);
        cultivator.setCounterCount(CounterType.OIL, oilCounters);
        gd.playerBattlefields.get(player.getId()).add(cultivator);
        return cultivator;
    }

    private Permanent addTappedForest(Player player) {
        Permanent forest = new Permanent(new Forest());
        forest.tap();
        gd.playerBattlefields.get(player.getId()).add(forest);
        return forest;
    }
}
