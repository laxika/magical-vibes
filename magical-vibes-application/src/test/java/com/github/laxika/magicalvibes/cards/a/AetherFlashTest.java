package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.j.JolraelsCentaur;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.w.Witchstalker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherFlash.class, GrizzlyBears.class, HillGiant.class, HornedTurtle.class,
        JolraelsCentaur.class, Witchstalker.class, Opalescence.class})
class AetherFlashTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering with 2 or less toughness is destroyed by the 2 damage")
    void destroysSmallEnteringCreature() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}"); // 2/2

        harness.passBothPriorities(); // resolve creature spell → Aether Flash triggers
        harness.passBothPriorities(); // resolve trigger → 2 damage → lethal to a 2/2

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tougher creature survives but keeps 2 marked damage")
    void toughCreatureSurvivesWithMarkedDamage() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}"); // 3/3

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → 2 damage marked

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tougher creature survives but keeps 2 marked damage")
    void toughCreatureSurvivesWithMarkedDamageUpstreamReview() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.castFromHand(player1, new HornedTurtle(), "{2}{U}");

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → 2 damage marked

        Permanent hornedTurtle = findPermanent(player1, "Horned Turtle");
        assertThat(hornedTurtle.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fires for a creature entering under an opponent's control")
    void firesForOpponentCreature() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → 2 damage → lethal to a 2/2

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed(Witchstalker.class)
    @DisplayName("Deals damage to an entering creature with hexproof")
    void damagesHexproofEnteringCreature() {
        harness.addToBattlefield(player1, new AetherFlash());

        Permanent witchstalker = harness.enterBattlefieldAndReturn(player2, new Witchstalker());
        harness.passBothPriorities();

        assertThat(witchstalker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @CardUsed(JolraelsCentaur.class)
    @DisplayName("Damages an entering creature with shroud because the trigger does not target it")
    void damagesEnteringCreatureWithShroud() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new JolraelsCentaur(), "{1}{G}{G}");

        harness.passBothPriorities(); // resolve creature spell → Aether Flash triggers
        harness.passBothPriorities(); // resolve trigger → 2 damage is dealt despite shroud

        harness.assertInGraveyard(player2, "Jolrael's Centaur");
    }

    @Test
    @DisplayName("Each Aether Flash deals damage independently to an entering creature")
    void multipleFlashesDealDamageIndependently() {
        harness.addToBattlefield(player1, new AetherFlash());
        harness.addToBattlefield(player2, new AetherFlash());

        harness.castFromHand(player1, new HornedTurtle(), "{2}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Horned Turtle");
        harness.assertInGraveyard(player1, "Horned Turtle");
    }

    @Test
    @DisplayName("Aether Flash entering does not damage creatures already on the battlefield")
    void enteringFlashDoesNotDamageExistingCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new AetherFlash(), "{2}{R}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aether Flash");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An Aether Flash entering as a creature triggers for its own entry")
    void animatedFlashDamagesItselfOnEntry() {
        harness.addToBattlefield(player1, new Opalescence());

        Permanent flash = harness.enterBattlefieldAndReturn(player1, new AetherFlash());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aether Flash");
        assertThat(flash.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Existing and entering Aether Flashes both damage an enchantment entering as a creature")
    void flashesDamageEnchantmentsEnteringAsCreatures() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new AetherFlash());

        harness.enterBattlefieldAndReturn(player2, new AetherFlash());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Aether Flash");
        harness.assertInGraveyard(player2, "Aether Flash");
        assertThat(findPermanent(player1, "Aether Flash").getMarkedDamage()).isZero();
    }
}
