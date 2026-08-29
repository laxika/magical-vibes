package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalmOfRestoration.class, Aeolipile.class, IcatianInfantry.class})
class BalmOfRestorationTest extends BaseCardTest {

    @Test
    void lifeGainModeGainsTwoLifeAndSacrificesBalm() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
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

    @Test
    void preventionModePreventsOnlyTheNextTwoDamageToTarget() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addToBattlefield(player1, new Aeolipile());
        harness.addToBattlefield(player1, new Aeolipile());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void preventionModeWorksForTargetCreature() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addToBattlefield(player1, new Aeolipile());
        harness.addToBattlefield(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        var target = findPermanent(player2, "Icatian Infantry");
        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        harness.addToBattlefield(player1, new BalmOfRestoration());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        findPermanent(player1, "Balm of Restoration").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
