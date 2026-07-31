package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForesightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving searches the library and exiles the three chosen cards")
    void exilesThreeChosenCards() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        chooseFirstCard();
        chooseFirstCard();
        chooseFirstCard();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.exiledCards.stream().map(ExiledCardEntry::card).map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp", "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Foresight");
    }

    @Test
    @DisplayName("Resolving schedules a draw at the beginning of the next turn's upkeep")
    void schedulesUpkeepDraw() {
        setupAndCast();

        harness.passBothPriorities();
        chooseFirstCard();
        chooseFirstCard();
        chooseFirstCard();

        GameData gd = harness.getGameData();
        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw happens at the next upkeep, even on the opponent's turn")
    void drawsAtNextUpkeep() {
        setupAndCast();

        harness.passBothPriorities();
        chooseFirstCard();
        chooseFirstCard();
        chooseFirstCard();

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).add(new Plains());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("A library with fewer than three cards exiles what it can")
    void shortLibraryExilesWhatItCan() {
        harness.setHand(player1, List.of(new Foresight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new Plains());

        harness.passBothPriorities();
        chooseFirstCard();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(ExiledCardEntry::card).map(Card::getName))
                .containsExactly("Plains");
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    private void chooseFirstCard() {
        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Foresight()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("The search is unrestricted: cards are not revealed and it cannot fail to find")
    void unrestrictedSearch() {
        setupAndCast();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();
    }
}
