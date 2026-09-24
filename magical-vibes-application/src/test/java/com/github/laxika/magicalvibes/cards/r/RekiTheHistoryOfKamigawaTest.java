package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.a.AyumiTheLastVisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RekiTheHistoryOfKamigawa.class, AyumiTheLastVisitor.class, ArabaMothrider.class})
class RekiTheHistoryOfKamigawaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a legendary spell draws a card")
    void legendarySpellDrawsCard() {
        harness.addToBattlefield(player1, new RekiTheHistoryOfKamigawa());
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.setHand(player1, List.of(new AyumiTheLastVisitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Casting a nonlegendary spell does not draw a card")
    void nonLegendarySpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new RekiTheHistoryOfKamigawa());
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.setHand(player1, List.of(new ArabaMothrider()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Araba Mothrider");
    }

    @Test
    @DisplayName("Casting a legendary spell by an opponent does not draw a card")
    void opponentCastingLegendarySpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new RekiTheHistoryOfKamigawa());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.setHand(player2, List.of(new AyumiTheLastVisitor()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Araba Mothrider");
    }
}
