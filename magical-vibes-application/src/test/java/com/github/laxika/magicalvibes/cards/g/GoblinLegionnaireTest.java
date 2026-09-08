package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinLegionnaire.class, GrizzlyBears.class, Shock.class})
class GoblinLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability sacrifices Goblin Legionnaire and deals 2 damage to a creature")
    void redAbilityDealsDamageAndSacrificesItself() {
        harness.addToBattlefield(player1, new GoblinLegionnaire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Legionnaire");
        harness.assertInGraveyard(player1, "Goblin Legionnaire");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("White ability prevents the next 2 damage to a creature")
    void whiteAbilityPreventsNextTwoDamage() {
        harness.addToBattlefield(player1, new GoblinLegionnaire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(findPermanent(player2, "Grizzly Bears").getDamagePreventionShield()).isZero();
    }
}
