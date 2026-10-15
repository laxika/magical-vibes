package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Bayou.class)
class BayouTest extends BaseCardTest {

    @Test
    @DisplayName("Bayou produces black mana")
    void producesBlackMana() {
        Permanent bayou = addBayouReady();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bayou.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bayou produces green mana")
    void producesGreenMana() {
        Permanent bayou = addBayouReady();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bayou.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bayou can produce mana immediately after entering and does not use the stack")
    void producesManaImmediatelyAfterEntering() {
        Permanent bayou = harness.enterBattlefieldAndReturn(player1, new Bayou());

        assertThat(bayou.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(bayou.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bayou cannot produce both colors from a single tap")
    void cannotActivateAgainWhileTapped() {
        Permanent bayou = harness.addToBattlefieldAndReturn(player1, new Bayou());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent is already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(bayou.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBayouReady() {
        Permanent bayou = harness.addToBattlefieldAndReturn(player1, new Bayou());
        bayou.setSummoningSick(false);
        return bayou;
    }
}
