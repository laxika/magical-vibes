package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Workhorse.class)
class WorkhorseTest extends BaseCardTest {

    @Test
    @DisplayName("Workhorse enters with four +1/+1 counters")
    void entersWithFourPlusOneCounters() {
        harness.setHand(player1, List.of(new Workhorse()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent workhorse = findPermanent(player1, "Workhorse");
        assertThat(workhorse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, workhorse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, workhorse)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter adds one colorless mana")
    void removesCounterForColorlessMana() {
        Permanent workhorse = addReadyWorkhorse(player1, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(workhorse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        addReadyWorkhorse(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("The mana ability can be activated while Workhorse is summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent workhorse = harness.addToBattlefieldAndReturn(player1, new Workhorse());
        workhorse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(workhorse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyWorkhorse(Player player, int counters) {
        Permanent workhorse = addCreatureReady(player, new Workhorse());
        workhorse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return workhorse;
    }
}
