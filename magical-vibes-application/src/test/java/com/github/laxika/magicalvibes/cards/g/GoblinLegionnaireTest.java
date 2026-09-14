package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RazorfinHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinLegionnaire.class, RazorfinHunter.class})
class GoblinLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability sacrifices Goblin Legionnaire and deals 2 damage to a creature")
    void redAbilityDealsDamageAndSacrificesItself() {
        harness.addToBattlefield(player1, new GoblinLegionnaire());
        harness.addToBattlefield(player2, new RazorfinHunter());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Razorfin Hunter");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Legionnaire");
        harness.assertInGraveyard(player1, "Goblin Legionnaire");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Razorfin Hunter");
        harness.assertInGraveyard(player2, "Razorfin Hunter");
    }

    @Test
    @DisplayName("Red ability can deal 2 damage to a player")
    void redAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GoblinLegionnaire());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("White ability prevents the next 2 damage to a creature across separate damage events")
    void whiteAbilityPreventsNextTwoDamage() {
        harness.addToBattlefield(player1, new GoblinLegionnaire());
        addCreatureReady(player1, new RazorfinHunter());
        addCreatureReady(player1, new RazorfinHunter());
        harness.addToBattlefield(player2, new RazorfinHunter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent target = findPermanent(player2, "Razorfin Hunter");
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Goblin Legionnaire");
        harness.assertInGraveyard(player1, "Goblin Legionnaire");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("White ability's prevention shield expires at end of turn")
    void whiteAbilityShieldExpiresAtEndOfTurn() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GoblinLegionnaire());
        addCreatureReady(player1, new RazorfinHunter());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
