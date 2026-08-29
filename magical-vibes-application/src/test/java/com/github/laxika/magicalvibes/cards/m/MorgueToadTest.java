package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MorgueToadTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Morgue Toad adds one blue and one red mana")
    void sacrificingAddsBlueAndRedMana() {
        harness.addToBattlefield(player1, new MorgueToad());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Morgue Toad");
        harness.assertInGraveyard(player1, "Morgue Toad");
    }
}
