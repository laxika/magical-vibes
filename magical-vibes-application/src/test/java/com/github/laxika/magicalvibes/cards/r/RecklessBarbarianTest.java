package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RecklessBarbarian.class)
class RecklessBarbarianTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Reckless Barbarian adds two red mana")
    void sacrificeAddsTwoRedMana() {
        harness.addToBattlefield(player1, new RecklessBarbarian());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Reckless Barbarian");
    }
}
