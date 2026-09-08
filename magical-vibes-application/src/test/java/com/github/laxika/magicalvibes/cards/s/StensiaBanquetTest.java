package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class StensiaBanquetTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to your Vampires and draws a card")
    void dealsDamageAndDrawsCard() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Counts only Vampires controlled by the spell's controller")
    void countsOnlyControllerVampires() {
        harness.addToBattlefield(player1, new CaptivatingVampire());
        harness.addToBattlefield(player2, new CaptivatingVampire());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new StensiaBanquet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Shock");
    }
}
