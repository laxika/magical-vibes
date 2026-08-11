package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class MetathranAerostatTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature with mana value X onto the battlefield and returns the source")
    void putsExactManaValueCreatureAndReturnsSource() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Metathran Aerostat");
    }

    @Test
    @DisplayName("Does not offer a creature whose mana value differs from X")
    void requiresExactManaValue() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Metathran Aerostat");
        harness.assertInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining the optional creature leaves the source and card in place")
    void mayDecline() {
        harness.addToBattlefield(player1, new MetathranAerostat());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Metathran Aerostat");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
