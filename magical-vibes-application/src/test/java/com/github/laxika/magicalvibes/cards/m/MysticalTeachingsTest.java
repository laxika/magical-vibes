package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MysticalTeachings.class, Cancel.class, AmbushViper.class, GrizzlyBears.class})
class MysticalTeachingsTest extends BaseCardTest {

    @Test
    @DisplayName("Search offers instant cards and cards with flash")
    void searchOffersInstantsAndFlashCards() {
        Card instant = new Cancel();
        Card flashCreature = new AmbushViper();
        Card creature = new GrizzlyBears();
        castFromHand(List.of(instant, flashCreature, creature));

        GameData gd = harness.getGameData();
        harness.passBothPriorities();

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(instant, flashCreature);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and finishes the search")
    void choosingMatchingCardPutsItIntoHand() {
        Card instant = new Cancel();
        Card flashCreature = new AmbushViper();
        castFromHand(List.of(instant, flashCreature));

        GameData gd = harness.getGameData();
        harness.passBothPriorities();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(flashCreature)));

        assertThat(gd.playerHands.get(player1.getId())).contains(flashCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback searches and exiles Mystical Teachings after resolution")
    void flashbackSearchesAndExiles() {
        MysticalTeachings teachings = new MysticalTeachings();
        Card instant = new Cancel();
        harness.setGraveyard(player1, List.of(teachings));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(instant);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        var search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.getPlayerExiledCards(player1.getId())).contains(teachings);
        assertThat(gameData.playerGraveyards.get(player1.getId())).doesNotContain(teachings);
    }

    private void castFromHand(List<Card> library) {
        harness.setHand(player1, List.of(new MysticalTeachings()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(library);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
    }
}
