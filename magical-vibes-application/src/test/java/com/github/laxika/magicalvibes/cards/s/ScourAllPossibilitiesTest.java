package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ScourAllPossibilities.class)
class ScourAllPossibilitiesTest extends BaseCardTest {

    @Test
    void scriesTwoThenDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castFromHand();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    void scryCanPutBothCardsOnBottomBeforeDrawing() {
        castFromHand();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card thirdCard = deck.get(2);

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(thirdCard);
    }

    @Test
    void flashbackScriesDrawsAndExilesSpell() {
        ScourAllPossibilities spell = new ScourAllPossibilities();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Scour All Possibilities");
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new ScourAllPossibilities()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
    }
}
