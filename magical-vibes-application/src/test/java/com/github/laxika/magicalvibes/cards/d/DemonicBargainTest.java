package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicBargain.class, GrizzlyBears.class, Plains.class, Swamp.class})
class DemonicBargainTest extends BaseCardTest {

    @Test
    void exilesTopThirteenCardsThenSearchesForACard() {
        Card bargain = new DemonicBargain();
        Card chosenCard = new Swamp();
        Card remainingCard = new Plains();
        List<Card> exiledCards = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            exiledCards.add(new GrizzlyBears());
        }

        harness.setHand(player1, List.of(bargain));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setLibrary(exiledCards, remainingCard, chosenCard);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(exiledCards);
        assertThat(search.params().cards()).containsExactly(remainingCard, chosenCard);

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(chosenIndex));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bargain);
    }

    private void setLibrary(List<Card> topCards, Card... remainingCards) {
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());
        library.clear();
        library.addAll(topCards);
        library.addAll(List.of(remainingCards));
    }
}
