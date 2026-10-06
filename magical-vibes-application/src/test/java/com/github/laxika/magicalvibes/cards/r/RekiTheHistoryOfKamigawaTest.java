package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.a.AyumiTheLastVisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RekiTheHistoryOfKamigawa.class, AyumiTheLastVisitor.class, ArabaMothrider.class})
class RekiTheHistoryOfKamigawaTest extends BaseCardTest {

    @Test
    @DisplayName("The draw resolves before the legendary spell and draws exactly one card")
    void drawResolvesBeforeLegendarySpell() {
        harness.addToBattlefield(player1, new RekiTheHistoryOfKamigawa());
        harness.setLibrary(player1, List.of(new ArabaMothrider(), new ArabaMothrider()));
        harness.setHand(player1, List.of(new AyumiTheLastVisitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        harness.assertNotInHand(player1, "Araba Mothrider");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Araba Mothrider");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ayumi, the Last Visitor");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ayumi, the Last Visitor");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Reki does not trigger when it is itself cast")
    void castingRekiDoesNotDrawCard() {
        harness.setLibrary(player1, List.of(new ArabaMothrider()));
        harness.setHand(player1, List.of(new RekiTheHistoryOfKamigawa()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Reki, the History of Kamigawa");
        harness.assertNotInHand(player1, "Araba Mothrider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

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
