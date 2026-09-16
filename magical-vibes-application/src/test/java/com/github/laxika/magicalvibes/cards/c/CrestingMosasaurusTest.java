package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThrashOfRaptors;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({CrestingMosasaurus.class, BeaconOfUnrest.class, GloriousAnthem.class,
        GrizzlyBears.class, ThrashOfRaptors.class})
class CrestingMosasaurusTest extends BaseCardTest {

    @Test
    void castEtbReturnsNonDinosaurCreaturesAndSpareDinosaursAndNoncreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ThrashOfRaptors());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.setHand(player1, List.of(new CrestingMosasaurus()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cresting Mosasaurus");
        harness.assertOnBattlefield(player1, "Thrash of Raptors");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void emergeSacrificesCreatureAndCastEtbReturnsNonDinosaurCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID sacrificedId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new ThrashOfRaptors());

        harness.setHand(player1, List.of(new CrestingMosasaurus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cresting Mosasaurus");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Thrash of Raptors");
    }

    @Test
    void enteringWithoutBeingCastDoesNotReturnCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CrestingMosasaurus()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Cresting Mosasaurus");
    }
}
