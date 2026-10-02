package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KorDuelist;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MistveilPlains;
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

@CardUsed({AridMesa.class, Mountain.class, Plains.class, Forest.class, Island.class, KorDuelist.class})
class AridMesaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Arid Mesa pays 1 life and sacrifices it")
    void activationPaysLifeAndSacrificesIt() {
        harness.addToBattlefield(player1, new AridMesa());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Arid Mesa");
        harness.assertInGraveyard(player1, "Arid Mesa");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Search offers only Mountain or Plains cards for the untapped battlefield")
    void searchOffersMountainOrPlains() {
        activateSearch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Mountain") || card.getName().equals("Plains"))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        gd.playerDecks.get(player1.getId()).get(0),
                        gd.playerDecks.get(player1.getId()).get(1)));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Mountain or Plains enters the battlefield untapped")
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
    @DisplayName("Player may fail to find with Arid Mesa")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mountain")
                        || permanent.getCard().getName().equals("Plains"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Arid Mesa cannot activate its search ability")
    void tappedMesaCannotActivate() {
        harness.addToBattlefield(player1, new AridMesa());
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Arid Mesa");
        harness.assertNotInGraveyard(player1, "Arid Mesa");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chosen Plains enters under the activating player's control and leaves the library")
    void chosenPlainsEntersUnderControllersControl() {
        activateSearch();
        harness.setLibrary(player2, List.of(new Mountain()));
        var opponentsLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        var plains = gd.playerDecks.get(player1.getId()).get(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(plains.getId()) && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(plains);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentsLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library resolves without putting a land onto the battlefield")
    void emptyLibraryResolves() {
        harness.addToBattlefield(player1, new AridMesa());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
        harness.assertInGraveyard(player1, "Arid Mesa");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without Mountain or Plains cards leaves its cards in the library")
    void noMatchingLandResolves() {
        harness.addToBattlefield(player1, new AridMesa());
        var forest = new Forest();
        var island = new Island();
        var creature = new KorDuelist();
        harness.setLibrary(player1, List.of(forest, island, creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island, creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({MistveilPlains.class})
    @DisplayName("Arid Mesa can find a nonbasic Plains and respects its enters-tapped ability")
    void findsNonbasicPlainsWithItsOwnEntryCondition() {
        harness.addToBattlefield(player1, new AridMesa());
        var land = new MistveilPlains();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new AridMesa());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Mountain(), new Plains(), new Forest(), new Island(), new KorDuelist()));
    }
}
