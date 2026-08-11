package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DromarsAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Paying Dromar's Attendant's ability sacrifices it and adds white, blue, and black mana")
    void activateAbilityAddsWhiteBlueAndBlackMana() {
        harness.addToBattlefield(player1, new DromarsAttendant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Dromar's Attendant");
        harness.assertInGraveyard(player1, "Dromar's Attendant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dromar's Attendant can activate its non-tap mana ability while summoning sick")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DromarsAttendant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
