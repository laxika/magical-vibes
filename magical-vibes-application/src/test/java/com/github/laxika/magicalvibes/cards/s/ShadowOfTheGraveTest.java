package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ShadowOfTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a cycled card but leaves a card that reached the graveyard by other means")
    void returnsCycledCardOnly() {
        // Grizzly Bears is already in the graveyard (put there by some other means, e.g. milled).
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Censor(), new ShadowOfTheGrave()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);      // cycling {U}
        harness.addMana(player1, ManaColor.BLACK, 1);     // Shadow of the Grave {1}{B}
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);    // cycle Censor -> graveyard
        harness.passBothPriorities();                     // resolve the cycling draw

        harness.castInstant(player1, 0);                  // Shadow of the Grave is now first in hand
        harness.passBothPriorities();

        harness.assertInHand(player1, "Censor");
        harness.assertNotInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns nothing when no card was cycled or discarded this turn")
    void returnsNothingWithoutDiscard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ShadowOfTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
