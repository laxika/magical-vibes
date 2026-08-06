package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EssenceBottleTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts an elixir counter on the bottle")
    void firstAbilityAddsElixirCounter() {
        Permanent bottle = addReadyBottle(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability removes all elixir counters and gains 2 life for each")
    void secondAbilityGainsTwoLifePerCounter() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        // Counters are removed immediately as a cost
        assertThat(bottle.getCounterCount(CounterType.ELIXIR)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 6);
    }

    @Test
    @DisplayName("Second ability with no elixir counters gains no life")
    void secondAbilityWithNoCountersGainsNoLife() {
        addReadyBottle(player1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Both abilities require tapping — a tapped bottle cannot activate")
    void tappedBottleCannotActivate() {
        Permanent bottle = addReadyBottle(player1);
        bottle.setCounterCount(CounterType.ELIXIR, 2);
        bottle.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability cannot be activated without paying {3}")
    void firstAbilityRequiresThreeMana() {
        addReadyBottle(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBottle(Player player) {
        Permanent perm = new Permanent(new EssenceBottle());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
