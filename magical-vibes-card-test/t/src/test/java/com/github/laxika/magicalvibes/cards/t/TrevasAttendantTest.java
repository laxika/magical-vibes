package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrevasAttendant.class})
class TrevasAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Paying Treva's Attendant's ability sacrifices it and adds green, white, and blue mana")
    void activateAbilityAddsGreenWhiteAndBlueMana() {
        harness.addToBattlefield(player1, new TrevasAttendant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Treva's Attendant");
        harness.assertInGraveyard(player1, "Treva's Attendant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Treva's Attendant can activate its non-tap mana ability while summoning sick")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new TrevasAttendant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Treva's Attendant cannot activate without paying its generic cost")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new TrevasAttendant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Treva's Attendant");
        harness.assertNotInGraveyard(player1, "Treva's Attendant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Treva's Attendant accepts colored mana and adds mana only to its controller")
    void tappedAttendantCanActivateWithColoredMana() {
        harness.addToBattlefieldAndReturn(player1, new TrevasAttendant()).tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Treva's Attendant");
        harness.assertInGraveyard(player1, "Treva's Attendant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
