package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunderflockTest extends BaseCardTest {

    @Test
    void costsFullAmountWithoutAnElemental() {
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void costsLessByTheGreatestManaValueOfAnElementalYouControl() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void castEtbReturnsAllNonElementalCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunderflock");
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void enteringWithoutBeingCastDoesNotReturnCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Sunderflock()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
