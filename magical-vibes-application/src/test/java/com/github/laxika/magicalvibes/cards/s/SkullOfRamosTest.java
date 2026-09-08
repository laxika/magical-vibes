package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkullOfRamosTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one black mana")
    void tapAddsOneBlackMana() {
        harness.addToBattlefield(player1, new SkullOfRamos());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skull of Ramos");
    }

    @Test
    @DisplayName("Sacrifice ability adds one black mana and moves the artifact to the graveyard")
    void sacrificeAddsOneBlackMana() {
        harness.addToBattlefield(player1, new SkullOfRamos());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Skull of Ramos");
        harness.assertInGraveyard(player1, "Skull of Ramos");
    }
}
