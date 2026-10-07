package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sunderflock.class, AirElemental.class, BeaconOfUnrest.class,
        GloriousAnthem.class, GrizzlyBears.class, Unsummon.class})
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
        Sunderflock target = new Sunderflock();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, target.getName());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void multipleElementalsUseTheGreatestValueRatherThanTheirSum() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opposingElementalsAndNonElementalsDoNotReduceCost() {
        harness.addToBattlefield(player2, new Sunderflock());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reductionCannotRemoveTheTwoBlueManaRequirement() {
        harness.addToBattlefield(player1, new Sunderflock());
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void triggerStillReturnsCreaturesAfterSunderflockLeaves() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sunderflock(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Sunderflock"));
        harness.assertInHand(player1, "Sunderflock");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @CardUsed({GatherSpecimens.class})
    void enteringUnderAnotherPlayersControlDoesNotTriggerTheBounce() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sunderflock()));
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player1, ManaColor.BLUE, 9);
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sunderflock");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
