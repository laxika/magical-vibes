package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DauntlessDourbark;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreefolkHarbinger.class, DauntlessDourbark.class, Forest.class,
        GoldmeadowHarrier.class, Island.class, Lignify.class, WoodlandChangeling.class})
class TreefolkHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability only offers Treefolk or Forest cards")
    void acceptingMayOffersTreefolkOrForest() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.TREEFOLK)
                        || c.getSubtypes().contains(CardSubtype.FOREST));
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the library")
    void choosingPutsItOnTop() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A Forest can be revealed and put on top")
    void canFindForest() {
        assertCanFind(new Forest());
    }

    @Test
    @DisplayName("A noncreature Treefolk card can be revealed and put on top")
    void canFindKindredEnchantment() {
        assertCanFind(new Lignify());
    }

    @Test
    @DisplayName("Changeling applies to Treefolk searches in the library")
    void canFindChangeling() {
        assertCanFind(new WoodlandChangeling());
    }

    @Test
    @DisplayName("The search may fail to find even when matching cards exist")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A library with no matching cards still gets searched and shuffled")
    void noMatchingCards() {
        setupAndCast();
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Searching an empty library completes the ability")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void assertCanFind(Card card) {
        setupAndCast();
        Island island = new Island();
        harness.setLibrary(player1, List.of(island, card));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(card);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals " + card.getName())).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new TreefolkHarbinger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new DauntlessDourbark(), new Forest(),
                new GoldmeadowHarrier(), new Island()));
    }
}
