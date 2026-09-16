package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RootSliver.class, Cancel.class, MetallicSliver.class, GrizzlyBears.class, ArcaneAdaptation.class})
class RootSliverTest extends BaseCardTest {

    @Test
    void rootSliverCannotBeCountered() {
        RootSliver rootSliver = new RootSliver();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, rootSliver, "{3}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rootSliver.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Root Sliver");
        harness.assertNotInGraveyard(player1, "Root Sliver");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void sliverSpellsCannotBeCounteredForAnyPlayer() {
        harness.addToBattlefield(player1, new RootSliver());

        MetallicSliver sliver = new MetallicSliver();
        harness.setHand(player2, List.of(sliver));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, sliver, "{1}");
        harness.passPriority(player2);
        harness.castInstant(player1, 0, sliver.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Metallic Sliver");
        harness.assertNotInGraveyard(player2, "Metallic Sliver");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    void nonSliverSpellsCanStillBeCountered() {
        harness.addToBattlefield(player1, new RootSliver());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    void allZoneSubtypeGrantMakesCreatureSpellUncounterable() {
        harness.addToBattlefield(player1, new RootSliver());
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.SLIVER);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }
}
