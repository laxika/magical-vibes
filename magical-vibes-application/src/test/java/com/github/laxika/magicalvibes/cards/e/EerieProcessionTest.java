package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EerieProcession.class, DampenThought.class, Plains.class})
class EerieProcessionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only Arcane cards, revealed and able to fail to find")
    void offersOnlyArcaneCards() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards()).allMatch(c -> c.getSubtypes().contains(CardSubtype.ARCANE));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().shuffleAfterSelection()).isTrue();
    }

    @Test
    @DisplayName("Choosing the Arcane card puts it into hand")
    void chosenArcaneCardGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, 0);

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

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Dampen Thought");
    }

    @Test
    @DisplayName("No Arcane cards in library gives no prompt")
    void noArcaneCardsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Eerie Procession");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new EerieProcession(), "{2}{U}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new DampenThought(), new Plains(), new Plains()));
    }
}
