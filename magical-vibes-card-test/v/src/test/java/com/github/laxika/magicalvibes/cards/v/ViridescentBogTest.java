package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridescentBog.class})
class ViridescentBogTest extends BaseCardTest {

    @Test
    @DisplayName("Paying one and tapping Viridescent Bog adds black and green mana")
    void addsBlackAndGreenMana() {
        Permanent bog = addReadyBog();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bog.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot activate Viridescent Bog without paying one")
    void cannotActivateWithoutMana() {
        addReadyBog();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate Viridescent Bog while tapped")
    void cannotActivateWhileTapped() {
        addReadyBog();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @EnumSource(ManaColor.class)
    @DisplayName("Any mana type can pay the generic activation cost")
    void acceptsAnyManaTypeForActivationCost(ManaColor paymentColor) {
        Permanent bog = addReadyBog();
        harness.addMana(player1, paymentColor, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bog.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            int expected = color == ManaColor.BLACK || color == ManaColor.GREEN ? 1 : 0;
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(expected);
            assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("Viridescent Bog can activate the turn it enters the battlefield")
    void canActivateTheTurnItEnters() {
        Permanent bog = harness.enterBattlefieldAndReturn(player1, new ViridescentBog());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(bog.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyBog() {
        return addCreatureReady(player1, new ViridescentBog());
    }
}
