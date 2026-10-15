package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TransguildPromenade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeekTheHorizon.class, Forest.class, Island.class, CentaurHealer.class, TransguildPromenade.class})
class SeekTheHorizonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic land cards, revealed")
    void searchShowsOnlyBasicLands() {
        setupSpell();
        List<Card> library = setupLibrary(2);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Three basic lands can be picked into hand")
    void picksThreeLandsIntoHand() {
        setupSpell();
        setupLibrary(4);

        harness.castAndResolveSorcery(player1, 0, 0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Search is up to three - can stop early")
    void canFailToFindEarly() {
        setupSpell();
        setupLibrary(4);

        harness.castAndResolveSorcery(player1, 0, 0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Search is up to three - can choose none")
    void canChooseNone() {
        setupSpell();
        setupLibrary(4);

        harness.castAndResolveSorcery(player1, 0, 0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No basic lands in library means no search prompt")
    void noBasicLandsNoPrompt() {
        setupSpell();
        harness.setLibrary(player1, List.of(new CentaurHealer(), new CentaurHealer()));

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Nonbasic lands are excluded from the search")
    void excludesNonbasicLands() {
        setupSpell();
        Forest forest = new Forest();
        TransguildPromenade nonbasic = new TransguildPromenade();
        harness.setLibrary(player1, List.of(nonbasic, forest));

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Fewer than three matching lands can all be taken, including duplicate names")
    void takesAllLandsFromShortLibrary() {
        setupSpell();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library still lets the spell finish resolving")
    void emptyLibraryResolves() {
        setupSpell();
        harness.setLibrary(player1, List.of());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setupSpell() {
        harness.setHand(player1, List.of(new SeekTheHorizon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Card> setupLibrary(int landCount) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < landCount; i++) {
            library.add(i % 2 == 0 ? new Forest() : new Island());
        }
        library.add(new CentaurHealer());
        harness.setLibrary(player1, library);
        return library;
    }
}
