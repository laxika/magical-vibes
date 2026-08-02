package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EerieProcessionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only Arcane cards, revealed and able to fail to find")
    void offersOnlyArcaneCards() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards()).allMatch(c -> c.getSubtypes().contains(CardSubtype.ARCANE));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing the Arcane card puts it into hand")
    void chosenArcaneCardGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Dampen Thought");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertInGraveyard(player1, "Eerie Procession");
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Dampen Thought");
    }

    @Test
    @DisplayName("No Arcane cards in library gives no prompt")
    void noArcaneCardsNoPrompt() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new Plains()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Eerie Procession");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new EerieProcession()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new DampenThought(), new GrizzlyBears(), new Plains()));
    }
}
