package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaggerCasterTest extends BaseCardTest {

    @Test
    void entersAndDealsDamageToEachOpponent() {
        harness.setLife(player2, 20);

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void entersAndDealsDamageToEachCreatureOpponentsControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void killsOpponentCreaturesWithOneToughness() {
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNotDamageCreaturesItsControllerControls() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, ownCreature);

        castDaggerCaster();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
    }

    private void castDaggerCaster() {
        harness.setHand(player1, List.of(new DaggerCaster()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }
}
