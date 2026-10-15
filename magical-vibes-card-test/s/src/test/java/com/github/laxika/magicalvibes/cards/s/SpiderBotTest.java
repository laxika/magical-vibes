package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderBot.class, Plains.class})
class SpiderBotTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability puts a chosen basic land on top of the library")
    void acceptsBasicLandSearch() {
        Plains plains = new Plains();
        SpiderBot otherBot = new SpiderBot();
        castSpiderBot(List.of(otherBot, plains));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards()).containsExactly(plains);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, otherBot);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void declinesBasicLandSearch() {
        Plains plains = new Plains();
        SpiderBot otherBot = new SpiderBot();
        castSpiderBot(List.of(plains, otherBot));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, otherBot);
    }

    @Test
    @DisplayName("An accepted search may fail to find even when a basic land is present")
    void mayFailToFindBasicLand() {
        Plains plains = new Plains();
        SpiderBot otherBot = new SpiderBot();
        castSpiderBot(List.of(plains, otherBot));
        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(plains, otherBot);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("An accepted search with no basic land finishes without moving a card")
    void searchWithoutBasicLands() {
        SpiderBot otherBot = new SpiderBot();
        castSpiderBot(List.of(otherBot));
        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(otherBot);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("An accepted search of an empty library finishes normally")
    void searchEmptyLibrary() {
        castSpiderBot(List.of());
        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private void castSpiderBot(List<Card> library) {
        harness.setHand(player1, List.of(new SpiderBot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
