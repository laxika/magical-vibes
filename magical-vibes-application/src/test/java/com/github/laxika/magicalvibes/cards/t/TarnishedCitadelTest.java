package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TarnishedCitadel.class)
class TarnishedCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} without dealing damage")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new TarnishedCitadel());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for any color adds the chosen mana and deals 3 damage")
    void tapForAnyColorMana() {
        harness.addToBattlefield(player1, new TarnishedCitadel());
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate either mana ability while Tarnished Citadel is tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new TarnishedCitadel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Each colored mana choice damages only the activating controller")
    void eachColorDamagesOnlyController(ManaColor color) {
        harness.addToBattlefield(player2, new TarnishedCitadel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, color.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana can be produced with less than three life because damage is not a cost")
    void canActivateWithLessThanThreeLife() {
        harness.addToBattlefield(player1, new TarnishedCitadel());
        harness.setLife(player1, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, -1);
        assertThat(gd.stack).isEmpty();
    }
}
