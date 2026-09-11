package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Armageddon;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Desertion.class, GrizzlyBears.class, Armageddon.class, WandOfDenial.class,
        IchorWellspring.class, Commandeer.class, Boomerang.class,
        DrogskolInfantry.class, DrogskolArmaments.class})
class DesertionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and puts it onto the battlefield under Desertion's controller")
    void countersCreatureAndGainsControl() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
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
        harness.castAndResolveInstant(player2, 0, wand.getId());

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Wand of Denial");
        harness.assertNotOnBattlefield(player1, "Wand of Denial");
        harness.assertNotInGraveyard(player1, "Wand of Denial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a non-artifact/creature spell into its owner's graveyard")
    void countersNoncreatureIntoGraveyard() {
        Armageddon armageddon = new Armageddon();
        harness.castFromHand(player1, armageddon, "{3}{W}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        int startingLife = harness.getGameData().getLife(player1.getId());

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, armageddon.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Armageddon");
        harness.assertNotOnBattlefield(player2, "Armageddon");
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Processes an artifact's enter-the-battlefield ability when Desertion puts it onto the battlefield")
    void processesArtifactEnterTheBattlefieldAbility() {
        IchorWellspring wellspring = new IchorWellspring();
        Armageddon cardToDraw = new Armageddon();
        harness.setLibrary(player2, List.of(cardToDraw));
        harness.castFromHand(player1, wellspring, "{2}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, wellspring.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ichor Wellspring");
        harness.assertInHand(player2, "Armageddon");
    }

    @Test
    @DisplayName("Puts a spell whose controller changed into its owner's graveyard")
    void countersControlledSpellIntoOwnersGraveyard() {
        Armageddon armageddon = new Armageddon();
        harness.castFromHand(player1, armageddon, "{3}{W}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castAndResolveInstant(player2, 0, armageddon.getId());

        harness.setHand(player1, List.of(new Desertion()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0, armageddon.getId());

        harness.assertInGraveyard(player1, "Armageddon");
        harness.assertNotInGraveyard(player2, "Armageddon");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Returns a Desertion-gained artifact to its owner when it leaves the battlefield")
    void returnsGainedArtifactToItsOwner() {
        WandOfDenial wand = new WandOfDenial();
        harness.castFromHand(player1, wand, "{2}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, wand.getId());

        UUID wandPermanentId = harness.getPermanentId(player2, "Wand of Denial");
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, wandPermanentId);

        harness.assertInHand(player1, "Wand of Denial");
        harness.assertNotInHand(player2, "Wand of Denial");
    }

    @Test
    @DisplayName("Exiles a spell cast with Disturb when it is countered")
    void countersDisturbSpellIntoExile() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        DrogskolInfantry infantry = new DrogskolInfantry();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(infantry));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0, bears.getId());

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, infantry.getId());

        harness.assertNotOnBattlefield(player2, "Drogskol Infantry");
        harness.assertNotInGraveyard(player1, "Drogskol Infantry");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(infantry.getId());
    }

    @Test
    @DisplayName("Fizzles if the target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new Desertion()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Desertion");
    }
}
