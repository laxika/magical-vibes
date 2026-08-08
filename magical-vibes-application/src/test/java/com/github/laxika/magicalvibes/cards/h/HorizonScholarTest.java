package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HorizonScholarTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Horizon Scholar enters the battlefield and offers scry 2")
    void etbOffersScryTwo() {
        harness.setHand(player1, List.of(new HorizonScholar()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Horizon Scholar");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 keeping both on top preserves library order")
    void scryBothOnTop() {
        harness.setHand(player1, List.of(new HorizonScholar()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(deck.get(0)).isSameAs(originalTop0);
        assertThat(deck.get(1)).isSameAs(originalTop1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 splitting top and bottom reorders the library")
    void scrySplitTopAndBottom() {
        harness.setHand(player1, List.of(new HorizonScholar()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(deck.get(0)).isSameAs(originalTop1);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop0);
    }
}
