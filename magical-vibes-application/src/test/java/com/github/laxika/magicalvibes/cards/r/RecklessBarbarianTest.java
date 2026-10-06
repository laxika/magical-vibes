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

    @Test
    @DisplayName("A tapped summoning-sick Barbarian can be sacrificed without using the stack")
    void tappedSummoningSickCreatureCanProduceManaImmediately() {
        var barbarian = harness.addToBattlefieldAndReturn(player1, new RecklessBarbarian());
        barbarian.setTapped(true);
        barbarian.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Reckless Barbarian");
        harness.assertInGraveyard(player1, "Reckless Barbarian");
    }

    @Test
    @DisplayName("The activating player receives the mana and sacrifices only their Barbarian")
    void manaGoesToActivatingPlayer() {
        harness.addToBattlefield(player1, new RecklessBarbarian());
        harness.addToBattlefield(player2, new RecklessBarbarian());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Reckless Barbarian");
        harness.assertNotOnBattlefield(player2, "Reckless Barbarian");
        harness.assertInGraveyard(player2, "Reckless Barbarian");
    }
}
