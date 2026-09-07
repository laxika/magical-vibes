package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Onulet.class, GrizzlyBears.class, WrathOfGod.class})
class OnuletTest extends BaseCardTest {

    @Test
    @DisplayName("Onulet dies from Wrath of God, controller gains 2 life")
    void diesFromWrathGainsLife() {
        harness.addToBattlefield(player1, new Onulet());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        int lifeBefore = gd.getLife(player1.getId());

        harness.passBothPriorities();

        // Onulet is dead
        harness.assertInGraveyard(player1, "Onulet");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An opponent's Onulet makes its controller gain 2 life when it dies")
    void opponentOnuletGivesLifeToItsController() {
        harness.addToBattlefield(player2, new Onulet());

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Onulet");

        harness.passBothPriorities();

        harness.assertLife(player1, player1LifeBefore);
        harness.assertLife(player2, player2LifeBefore + 2);
    }

    @Test
    @DisplayName("Onulet dies in combat, controller gains 2 life")
    void diesInCombatGainsLife() {
        Permanent onuletPerm = addCreatureReady(player1, new Onulet());
        onuletPerm.setBlocking(true);
        onuletPerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(3);
        bears.setToughness(3);
        Permanent attacker = addCreatureReady(player2, bears);
        attacker.setAttacking(true);

        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Onulet");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Onulet survives combat, no life gained")
    void survivesNoLifeGain() {
        Permanent onuletPerm = addCreatureReady(player1, new Onulet());
        onuletPerm.setBlocking(true);
        onuletPerm.addBlockingTarget(0);

        GrizzlyBears weakAttacker = new GrizzlyBears();
        weakAttacker.setPower(0);
        weakAttacker.setToughness(2);
        Permanent attacker = addCreatureReady(player2, weakAttacker);
        attacker.setAttacking(true);

        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Onulet");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
