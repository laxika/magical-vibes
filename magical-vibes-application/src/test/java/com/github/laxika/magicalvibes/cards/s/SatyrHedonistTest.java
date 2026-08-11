package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SatyrHedonistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself to add three red mana")
    void sacrificesItselfForThreeRedMana() {
        harness.addToBattlefield(player1, new SatyrHedonist());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Satyr Hedonist");
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void requiresRedMana() {
        harness.addToBattlefield(player1, new SatyrHedonist());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

}
