package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PsionicBlast.class, GrizzlyBears.class})
class PsionicBlastTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetCreatureAndTwoDamageToController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    void dealsFourDamageToTargetPlayerAndTwoDamageToController() {
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
    }

    @Test
    void dealsSixDamageWhenTargetingItsController() {
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 14);
    }

    @Test
    void dealsDamageToItsControllersCreatureAndStillDamagesController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new PsionicBlast()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotDamageControllerWhenOnlyTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Psionic Blast");
    }
}
