package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilvergladeElemental.class, Forest.class, Plains.class, Island.class, FreshVolunteers.class})
class SilvergladeElementalTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers the optional Forest search")
    void etbOffersOptionalForestSearch() {
        setupAndCast();

        resolveAllTriggers();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the ETB search presents only Forest cards for the battlefield")
    void acceptingSearchPresentsForestCardsForBattlefield() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.FOREST));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Forest enters the battlefield untapped")
    void chosenForestEntersBattlefieldUntapped() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSubtypes().contains(CardSubtype.FOREST)
                        && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the ETB search with no Forest puts no card onto the battlefield")
    void acceptingSearchWithNoForestFindsNothing() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new FreshVolunteers()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(3)
                .allMatch(card -> !card.getSubtypes().contains(CardSubtype.FOREST));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the ETB search does nothing")
    void decliningSearchSkipsLibrarySearch() {
        setupAndCast();
        setupLibrary();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find even when a Forest is available")
    void mayFailToFindAvailableForest() {
        setupAndCast();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Accepting the search with an empty library finishes normally")
    void acceptingSearchWithEmptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The search takes exactly one Forest from the controller's library")
    void searchesOnlyControllersLibraryForOneForest() {
        setupAndCast();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Forest opponentForest = new Forest();
        harness.setLibrary(player1, List.of(firstForest, secondForest));
        harness.setLibrary(player2, List.of(opponentForest));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondForest);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentForest);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstForest.getId())
                        && !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Declining the search preserves the library and does not shuffle")
    void decliningSearchPreservesLibraryOrder() {
        setupAndCast();
        Plains plains = new Plains();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(plains, forest, island));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isFalse();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new SilvergladeElemental(), "{4}{G}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new FreshVolunteers()));
    }
}
