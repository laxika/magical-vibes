package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Desertion.class, CloudElemental.class, Impulse.class, WandOfDenial.class,
        IchorWellspring.class, Commandeer.class, Boomerang.class})
class DesertionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and puts it onto the battlefield under Desertion's controller")
    void countersCreatureAndGainsControl() {
        CloudElemental elemental = new CloudElemental();
        harness.castFromHand(player1, elemental, "{2}{U}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, elemental.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // The countered creature enters under player2's control, not into player1's graveyard/library.
        harness.assertOnBattlefield(player2, "Cloud Elemental");
        harness.assertNotOnBattlefield(player1, "Cloud Elemental");
        harness.assertNotInGraveyard(player1, "Cloud Elemental");
        // player1 is still the owner (recorded so the card returns to them when it leaves play).
        assertThat(gd.stolenCreatures).containsValue(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters an artifact spell and puts it onto the battlefield under Desertion's controller")
    void countersArtifactAndGainsControl() {
        WandOfDenial wand = new WandOfDenial();
        harness.castFromHand(player1, wand, "{2}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, wand.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Wand of Denial");
        harness.assertNotOnBattlefield(player1, "Wand of Denial");
        harness.assertNotInGraveyard(player1, "Wand of Denial");
        assertThat(gd.stolenCreatures).containsValue(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a non-artifact/creature spell into its owner's graveyard")
    void countersNoncreatureIntoGraveyard() {
        Impulse impulse = new Impulse();
        harness.castFromHand(player1, impulse, "{1}{U}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        int startingLife = harness.getGameData().getLife(player1.getId());

        harness.passPriority(player1);
        harness.castInstant(player2, 0, impulse.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Impulse");
        harness.assertNotOnBattlefield(player2, "Impulse");
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Processes an artifact's enter-the-battlefield ability when Desertion puts it onto the battlefield")
    void processesArtifactEnterTheBattlefieldAbility() {
        IchorWellspring wellspring = new IchorWellspring();
        Impulse cardToDraw = new Impulse();
        harness.setLibrary(player2, List.of(cardToDraw));
        harness.castFromHand(player1, wellspring, "{2}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, wellspring.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ichor Wellspring");
        harness.assertInHand(player2, "Impulse");
    }

    @Test
    @DisplayName("Puts a spell whose controller changed into its owner's graveyard")
    void countersControlledSpellIntoOwnersGraveyard() {
        Impulse impulse = new Impulse();
        harness.castFromHand(player1, impulse, "{1}{U}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castInstant(player2, 0, impulse.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Desertion()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, impulse.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impulse");
        harness.assertNotInGraveyard(player2, "Impulse");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a Desertion-gained artifact to its owner when it leaves the battlefield")
    void returnsGainedArtifactToItsOwner() {
        WandOfDenial wand = new WandOfDenial();
        harness.castFromHand(player1, wand, "{2}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castInstant(player2, 0, wand.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Desertion()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, wand.getId());
        harness.passBothPriorities();

        UUID wandPermanentId = harness.getPermanentId(player1, "Wand of Denial");
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, wandPermanentId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wand of Denial");
        harness.assertNotInHand(player2, "Wand of Denial");
    }

    @Test
    @DisplayName("Fizzles if the target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        CloudElemental elemental = new CloudElemental();
        harness.castFromHand(player1, elemental, "{2}{U}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, elemental.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Cloud Elemental"));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloud Elemental");
        harness.assertInGraveyard(player2, "Desertion");
    }
}
