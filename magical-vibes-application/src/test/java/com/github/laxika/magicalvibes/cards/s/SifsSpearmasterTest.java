package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SifsSpearmaster.class)
class SifsSpearmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its power to target opponent")
    void dealsDamageEqualToPower() {
        Permanent spearmaster = addCreatureReady(player1, new SifsSpearmaster());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(spearmaster.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Uses the creature's effective power")
    void usesEffectivePower() {
        Permanent spearmaster = addCreatureReady(player1, new SifsSpearmaster());
        spearmaster.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        addCreatureReady(player1, new SifsSpearmaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
