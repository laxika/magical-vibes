package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalmOfRestorationTest extends BaseCardTest {

    @Test
    void lifeGainModeGainsTwoLifeAndSacrificesBalm() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInGraveyard(player1, "Balm of Restoration");
    }

    @Test
    void preventionModeTargetsAPlayerAndCreatesATwoDamageShield() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Balm of Restoration");
    }

    @Test
    void preventionModeRequiresATarget() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }
}
