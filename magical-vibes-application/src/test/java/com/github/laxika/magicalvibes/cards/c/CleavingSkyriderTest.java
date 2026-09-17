package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CleavingSkyrider.class, GrizzlyBears.class})
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
        var firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        var secondAttacker = addCreatureReady(player1, new GrizzlyBears());
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
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void kickedCanDealDamageToACreature() {
        var firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        var secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setPowerModifier(-2);
        secondAttacker.setPowerModifier(-2);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player2, new GrizzlyBears());
        var target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new CleavingSkyrider()));
        addMana(true);
        harness.castKickedCreature(player1, 0, target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addMana(boolean kicked) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, kicked ? 1 : 0);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 4 : 2);
    }
}
