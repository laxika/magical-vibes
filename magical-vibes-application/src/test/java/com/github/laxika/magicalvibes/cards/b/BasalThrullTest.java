package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BasalThrull.class)
class BasalThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Basal Thrull adds two black mana")
    void tapsAndSacrificesForTwoBlackMana() {
        addCreatureReady(player1, new BasalThrull());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Basal Thrull");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate Basal Thrull while tapped")
    void cannotActivateWhileTapped() {
        Permanent basalThrull = addCreatureReady(player1, new BasalThrull());
        basalThrull.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertOnBattlefield(player1, "Basal Thrull");
    }
}
