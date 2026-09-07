package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CoalGolem.class)
class CoalGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Paying three mana and sacrificing Coal Golem adds three red mana")
    void paysManaSacrificesAndAddsThreeRedMana() {
        harness.addToBattlefield(player1, new CoalGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Coal Golem");
        harness.assertInGraveyard(player1, "Coal Golem");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("The generic activation cost can be paid with colored mana")
    void paysGenericCostWithColoredMana() {
        harness.addToBattlefield(player1, new CoalGolem());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Coal Golem cannot be activated without three mana")
    void requiresThreeManaToActivate() {
        harness.addToBattlefield(player1, new CoalGolem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Coal Golem");
    }
}
