package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuccumbToTemptation.class})
class SuccumbToTemptationTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and loses 2 life")
    void drawsTwoCardsAndLosesLife() {
        harness.setHand(player1, List.of(new SuccumbToTemptation()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top0, top1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Succumb to Temptation");
    }

    @Test
    @DisplayName("Nonactive caster at 3 life draws two cards and falls to 1 life")
    void nonactiveCasterAtLowLifeDrawsAndLosesLife() {
        Card first = new SuccumbToTemptation();
        Card second = new SuccumbToTemptation();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player2, List.of(new SuccumbToTemptation()));
        harness.setHand(player1, List.of());
        harness.setLife(player2, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);
        int player1LibrarySize = gd.playerDecks.get(player1.getId()).size();

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1LibrarySize);
        harness.assertInGraveyard(player2, "Succumb to Temptation");
    }
}
