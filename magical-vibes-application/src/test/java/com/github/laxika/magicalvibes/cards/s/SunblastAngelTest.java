package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class SunblastAngelTest extends BaseCardTest {

    

    @Test
    @DisplayName("ETB destroys tapped creatures on both sides")
    void etbDestroysTappedCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        // Tap both creatures
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        Permanent elves = findPermanent(player2, "Llanowar Elves");
        elves.tap();

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("ETB does not destroy untapped creatures")
    void etbDoesNotDestroyUntappedCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sunblast Angel itself is untapped so does not destroy itself")
    void doesNotDestroyItself() {
        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunblast Angel");
    }

    @Test
    @DisplayName("Indestructible tapped creature survives ETB")
    void indestructibleTappedCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        bears.tap();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapped creature with regeneration shield can be regenerated")
    void tappedCreatureCanBeRegenerated() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        bears.tap();
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new SunblastAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        // Resolve creature spell → ETB triggers
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        // Creature should survive via regeneration since cannotBeRegenerated is false
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
