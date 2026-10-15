package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartHarbinger.class, BoggartMob.class, BoggartSpriteChaser.class, Mulldrifter.class,
        Island.class, Tarfire.class, AmoeboidChangeling.class})
class BoggartHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Boggart Harbinger creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        resolveMayPrompt();

        harness.assertOnBattlefield(player1, "Boggart Harbinger");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability only offers Goblin cards")
    void acceptingMayOffersOnlyGoblins() {
        setupAndCast();
        setupLibraryWithGoblins();

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().shuffleAfterSelection()).isTrue();
        assertThat(search.params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.GOBLIN));
    }

    @Test
    @DisplayName("Choosing a Goblin card puts it on top of the library")
    void choosingGoblinPutsItOnTop() {
        setupAndCast();
        setupLibraryWithGoblins();

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability with no Goblins finds no card")
    void acceptingMayWithNoGoblinsFindsNoCard() {
        setupAndCast();
        setupLibraryWithoutGoblins();

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mulldrifter", "Island");
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithGoblins();

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature Goblin card can be revealed and put on top")
    void searchesForNoncreatureGoblin() {
        setupAndCast();
        Card goblin = new Tarfire();
        Card other = new Island();
        harness.setLibrary(player1, List.of(other, goblin));

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(goblin);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(goblin, other);
        assertThat(gameLogContains("reveals Tarfire")).isTrue();
        harness.assertNotInHand(player1, "Tarfire");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A changeling in the library qualifies as a Goblin")
    void searchesForChangeling() {
        setupAndCast();
        Card changeling = new AmoeboidChangeling();
        Card other = new Mulldrifter();
        harness.setLibrary(player1, List.of(other, changeling));

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(changeling);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(changeling, other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even when a Goblin is present")
    void mayFailToFindExistingGoblin() {
        setupAndCast();
        Card goblin = new BoggartMob();
        Card other = new Island();
        harness.setLibrary(player1, List.of(goblin, other));

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(goblin, other);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Boggart Mob");
    }

    @Test
    @DisplayName("Accepting the ability with an empty library completes normally")
    void acceptsSearchOfEmptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Boggart Harbinger");
    }

    @Test
    @DisplayName("Declining the search preserves the exact library order")
    void decliningPreservesLibraryOrder() {
        setupAndCast();
        List<Card> library = List.of(new Mulldrifter(), new BoggartMob(), new Island(), new BoggartSpriteChaser());
        harness.setLibrary(player1, library);

        resolveMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new BoggartHarbinger(), "{2}{B}");
    }

    private void resolveMayPrompt() {
        resolveAllTriggers();
    }

    private void setupLibraryWithGoblins() {
        harness.setLibrary(player1, List.of(new BoggartMob(), new BoggartSpriteChaser(), new Mulldrifter(), new Island()));
    }

    private void setupLibraryWithoutGoblins() {
        harness.setLibrary(player1, List.of(new Mulldrifter(), new Island()));
    }
}
