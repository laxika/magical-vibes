package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnvironmentalScientist.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class EnvironmentalScientistTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers a basic land for the hand")
    void acceptingEnterAbilityOffersBasicLand() {
        setLibrary(new Forest(), new Island(), new GrizzlyBears());
        castEnvironmentalScientist();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card ->
                card.getName().equals("Forest") || card.getName().equals("Island"));
    }

    @Test
    @DisplayName("A restricted search may find no card even when a basic land is available")
    void canFailToFindAvailableBasicLand() {
        setLibrary(new Forest(), new EnvironmentalScientist());
        castEnvironmentalScientist();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Accepting a search with no basic lands still shuffles")
    void noBasicLandsStillShuffles() {
        setLibrary(new EnvironmentalScientist());
        castEnvironmentalScientist();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Accepting a search of an empty library completes normally")
    void emptyLibrarySearchCompletes() {
        setLibrary();
        castEnvironmentalScientist();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The found land is revealed and only the controller's library is searched")
    void revealsFoundLandAndLeavesOpponentLibraryAlone() {
        Forest forest = new Forest();
        Island island = new Island();
        setLibrary(forest, new EnvironmentalScientist());
        harness.setLibrary(player2, List.of(island));
        castEnvironmentalScientist();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(island);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Forest"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled."));
    }

    @Test
    @DisplayName("Declining the ETB ability skips the search")
    void decliningEnterAbilitySkipsSearch() {
        setLibrary(new Plains(), new GrizzlyBears());
        castEnvironmentalScientist();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getName().equals("Plains"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void castEnvironmentalScientist() {
        harness.setHand(player1, List.of(new EnvironmentalScientist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
