package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.j.JolraelsCentaur;
import com.github.laxika.magicalvibes.cards.w.Witchstalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherFlash.class, GrizzlyBears.class, HornedTurtle.class, JolraelsCentaur.class, Witchstalker.class})
class AetherFlashTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering with 2 or less toughness is destroyed by the 2 damage")
    void destroysSmallEnteringCreature() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.setHand(player1, List.of(new GrizzlyBears())); // 2/2
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Aether Flash triggers
        harness.passBothPriorities(); // resolve trigger → 2 damage → lethal to a 2/2

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tougher creature survives but keeps 2 marked damage")
    void toughCreatureSurvivesWithMarkedDamage() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.setHand(player1, List.of(new HornedTurtle())); // 1/4
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);

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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → 2 damage → lethal to a 2/2

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed(JolraelsCentaur.class)
    @DisplayName("Damages an entering creature with shroud because the trigger does not target it")
    void damagesEnteringCreatureWithShroud() {
        harness.addToBattlefield(player1, new AetherFlash());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new JolraelsCentaur()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell → Aether Flash triggers
        harness.passBothPriorities(); // resolve trigger → 2 damage is dealt despite shroud

        harness.assertInGraveyard(player2, "Jolrael's Centaur");
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
}
