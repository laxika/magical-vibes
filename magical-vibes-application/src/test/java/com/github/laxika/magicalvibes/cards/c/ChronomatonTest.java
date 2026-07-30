package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChronomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a +1/+1 counter on Chronomaton and taps it")
    void activationAddsCounter() {
        Permanent golem = addChronomatonReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(golem.isTapped()).isTrue();
        assertThat(golem.getEffectivePower()).isEqualTo(2);
        assertThat(golem.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters accumulate across activations")
    void countersAccumulate() {
        Permanent golem = addChronomatonReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        golem.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(golem.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chronomaton cannot activate its ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent golem = addChronomatonReady(player1);
        golem.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    private Permanent addChronomatonReady(Player player) {
        Permanent perm = new Permanent(new Chronomaton());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
