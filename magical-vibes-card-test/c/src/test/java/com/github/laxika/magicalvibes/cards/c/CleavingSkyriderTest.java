package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.y.YavimayaSteelcrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CleavingSkyrider.class, YavimayaSteelcrusher.class})
class CleavingSkyriderTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotDealDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(false);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void kickedDealsDamageEqualToNumberOfAttackingCreatures() {
        harness.setLife(player2, 20);
        var firstAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        var secondAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        firstAttacker.setPowerModifier(-2);
        secondAttacker.setPowerModifier(-2);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);
        harness.castKickedCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void kickedCanDealDamageToACreature() {
        var firstAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        var secondAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        firstAttacker.setPowerModifier(-2);
        secondAttacker.setPowerModifier(-2);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player2, new YavimayaSteelcrusher());
        var target = harness.getPermanentId(player2, "Yavimaya Steelcrusher");

        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);
        harness.castKickedCreature(player1, 0, target);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Yavimaya Steelcrusher");
        harness.assertInGraveyard(player2, "Yavimaya Steelcrusher");
    }

    @Test
    void kickedWithNoAttackersDealsNoDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);

        harness.castKickedCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cleaving Skyrider");
        harness.assertLife(player2, 20);
    }

    @Test
    void kickedCountsOpponentsAttackersWhenCastDuringTheirCombat() {
        harness.setLife(player2, 20);
        var attacker = addCreatureReady(player2, new YavimayaSteelcrusher());
        attacker.setPowerModifier(-2);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);

        harness.castKickedCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cleaving Skyrider");
        harness.assertLife(player2, 19);
    }

    @Test
    void damageCountIsEvaluatedWhenTheEnterTriggerResolves() {
        harness.setLife(player2, 20);
        var firstAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        var secondAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        firstAttacker.setPowerModifier(-2);
        secondAttacker.setPowerModifier(-2);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cleaving Skyrider");
        secondAttacker.setAttacking(false);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    void enteringWithoutBeingCastDoesNotDealDamage() {
        harness.setLife(player2, 20);
        var attacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        attacker.setAttacking(true);

        harness.enterBattlefieldAndReturn(player1, new CleavingSkyrider());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cleaving Skyrider");
        harness.assertLife(player2, 20);
    }

    @Test
    void kickedChoosesItsTargetAfterEnteringAndCanTargetItself() {
        var firstAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        var secondAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        firstAttacker.setPowerModifier(-2);
        secondAttacker.setPowerModifier(-2);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cleaving Skyrider");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Cleaving Skyrider"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cleaving Skyrider");
        harness.assertInGraveyard(player1, "Cleaving Skyrider");
    }

    private void addMana(boolean kicked) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, kicked ? 1 : 0);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 4 : 2);
    }
}
