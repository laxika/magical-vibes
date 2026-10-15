package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GladeWatcher;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.t.ThunderbreakRegent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhansTriumph.class, ThunderbreakRegent.class, GladeWatcher.class, Naturalize.class})
class SarkhansTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only Dragon creature cards")
    void offersOnlyDragonCreatures() {
        ThunderbreakRegent dragon = new ThunderbreakRegent();
        setUpAndCast(dragon, new GladeWatcher(), new Naturalize());

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactly(dragon);
    }

    @Test
    @DisplayName("Choosing a Dragon puts it into hand and shuffles the library")
    void choosingDragonPutsItIntoHand() {
        ThunderbreakRegent dragon = new ThunderbreakRegent();
        GladeWatcher nonDragon = new GladeWatcher();
        setUpAndCast(dragon, nonDragon);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals " + dragon.getName())
                        && entry.contains("puts it into their hand")
                        && entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("A library without a Dragon resolves without a search interaction")
    void noDragonFound() {
        GladeWatcher nonDragon = new GladeWatcher();
        setUpAndCast(nonDragon);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
    }

    @Test
    @DisplayName("The controller may fail to find even when a Dragon is available")
    void mayFailToFindAnAvailableDragon() {
        ThunderbreakRegent dragon = new ThunderbreakRegent();
        GladeWatcher nonDragon = new GladeWatcher();
        setUpAndCast(dragon, nonDragon);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(dragon, nonDragon);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Sarkhan's Triumph");
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a card in hand")
    void emptyLibraryResolves() {
        setUpAndCast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sarkhan's Triumph");
    }

    private void setUpAndCast(Card... libraryCards) {
        harness.setLibrary(player1, List.of(libraryCards));
        harness.setHand(player1, List.of(new SarkhansTriumph()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);
    }
}
