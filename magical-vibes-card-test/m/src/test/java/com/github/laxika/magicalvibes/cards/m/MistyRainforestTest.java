package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LotusCobra;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistyRainforest.class, Forest.class, Island.class, Mountain.class, Plains.class, LotusCobra.class})
class MistyRainforestTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Misty Rainforest pays 1 life and sacrifices it")
    void activationPaysLifeAndSacrificesIt() {
        harness.addToBattlefield(player1, new MistyRainforest());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Misty Rainforest");
        harness.assertInGraveyard(player1, "Misty Rainforest");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Search offers only Forest or Island cards for the untapped battlefield")
    void searchOffersForestOrIsland() {
        activateSearch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Forest") || card.getName().equals("Island"))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        gd.playerDecks.get(player1.getId()).get(0),
                        gd.playerDecks.get(player1.getId()).get(1)));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Forest or Island enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals(chosenName) && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find with Misty Rainforest")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest")
                        || permanent.getCard().getName().equals("Island"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Misty Rainforest cannot activate or pay its costs")
    void tappedRainforestCannotActivate() {
        harness.addToBattlefield(player1, new MistyRainforest());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Misty Rainforest");
        harness.assertNotInGraveyard(player1, "Misty Rainforest");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chosen Island leaves only the activating player's library and enters untapped")
    void chosenIslandEntersUnderControllersControl() {
        activateSearch();
        harness.setLibrary(player2, List.of(new Mountain()));
        var opponentsLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        var island = gd.playerDecks.get(player1.getId()).get(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(island.getId()) && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(island);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentsLibrary);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library search resolves after paying all costs")
    void emptyLibraryResolves() {
        harness.addToBattlefield(player1, new MistyRainforest());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Misty Rainforest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library without a Forest or Island retains all its cards")
    void noMatchingLandResolves() {
        harness.addToBattlefield(player1, new MistyRainforest());
        var mountain = new Mountain();
        var plains = new Plains();
        var creature = new LotusCobra();
        harness.setLibrary(player1, List.of(mountain, plains, creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(mountain, plains, creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BreedingPool.class})
    @DisplayName("Misty Rainforest finds nonbasic lands and respects their entry conditions")
    void findsNonbasicLandWithItsOwnEntryCondition() {
        harness.addToBattlefield(player1, new MistyRainforest());
        var pool = new BreedingPool();
        harness.setLibrary(player1, List.of(pool));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(pool);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(pool.getId()) && permanent.isTapped());
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new MistyRainforest());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains(), new LotusCobra()));
    }
}
