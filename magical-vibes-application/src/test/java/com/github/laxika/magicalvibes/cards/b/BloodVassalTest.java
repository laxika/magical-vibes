package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodVassalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Blood Vassal adds two black mana")
    void sacrificeAddsTwoBlackMana() {
        addCreatureReady(player1, new BloodVassal());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Blood Vassal");
    }
}
