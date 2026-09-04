package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.j.Jokulhaups;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tarpan.class, Jokulhaups.class, BalduvianBears.class})
class TarpanTest extends BaseCardTest {

    @Test
    @DisplayName("Tarpan dies from Jokulhaups, controller gains 1 life")
    void diesFromJokulhaupsGainsLife() {
        harness.addToBattlefield(player1, new Tarpan());

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());

        // Tarpan is dead
        harness.assertInGraveyard(player1, "Tarpan");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tarpan dies in combat, controller gains 1 life")
    void diesInCombatGainsLife() {
        Permanent tarpanPerm = addCreatureReady(player1, new Tarpan());
        tarpanPerm.setBlocking(true);
        tarpanPerm.addBlockingTarget(0);

        BalduvianBears attackerCard = new BalduvianBears();
        attackerCard.setPower(3);
        attackerCard.setToughness(3);
        Permanent attacker = addCreatureReady(player2, attackerCard);
        attacker.setAttacking(true);

        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Tarpan");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tarpan survives combat, no life gained")
    void survivesNoLifeGain() {
        Permanent tarpanPerm = addCreatureReady(player1, new Tarpan());
        tarpanPerm.setBlocking(true);
        tarpanPerm.addBlockingTarget(0);

        BalduvianBears attackerCard = new BalduvianBears();
        attackerCard.setPower(0);
        attackerCard.setToughness(2);
        Permanent attacker = addCreatureReady(player2, attackerCard);
        attacker.setAttacking(true);

        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Tarpan");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tarpan's controller gains life when an opponent destroys it")
    void controllerGainsLifeWhenOpponentDestroysTarpan() {
        harness.addToBattlefield(player2, new Tarpan());

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tarpan");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore + 1);
    }
}
