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

    @Test
    @DisplayName("Determines power when the ability resolves")
    void usesPowerAtResolution() {
        Permanent spearmaster = addCreatureReady(player1, new SifsSpearmaster());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        spearmaster.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Zero power deals no damage but still pays the tap cost")
    void zeroPowerDealsNoDamage() {
        Permanent spearmaster = addCreatureReady(player1, new SifsSpearmaster());
        spearmaster.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(spearmaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent spearmaster = addCreatureReady(player1, new SifsSpearmaster());
        spearmaster.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(spearmaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate again without untapping")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new SifsSpearmaster());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }
}
