package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SchemingSymmetry.class, GrizzlyBears.class, Island.class, Plains.class})
class SchemingSymmetryTest extends BaseCardTest {

    @Test
    @DisplayName("Each target player searches their own library and puts a chosen card on top")
    void eachTargetPlayerSearchesTheirOwnLibrary() {
        setupLibraries();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch firstSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch.params().playerId()).isEqualTo(player1.getId());
        assertThat(firstSearch.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Island");
        int bearsIndex = firstSearch.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Grizzly Bears");
        harness.handleCardChosen(player1, bearsIndex);

        PendingInteraction.LibrarySearch secondSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch.params().playerId()).isEqualTo(player2.getId());
        assertThat(secondSearch.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Plains");

        int plainsIndex = secondSearch.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Plains");
        harness.handleCardChosen(player2, plainsIndex);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Plains");
    }

    @Test
    @DisplayName("Requires two distinct player targets")
    void requiresTwoDistinctPlayerTargets() {
        harness.setHand(player1, List.of(new SchemingSymmetry()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithOnlyOnePlayerTarget() {
        harness.setHand(player1, List.of(new SchemingSymmetry()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void neitherPlayerCanDeclineAnUnrestrictedSearch() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Plains()));
        cast();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Scheming Symmetry");
    }

    @Test
    void emptyFirstLibraryDoesNotPreventSecondPlayerSearching() {
        harness.setLibrary(player1, List.of());
        Plains chosen = new Plains();
        harness.setLibrary(player2, List.of(chosen));
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Scheming Symmetry");
    }

    @Test
    void emptySecondLibraryDoesNotLeaveResolutionWaiting() {
        Island chosen = new Island();
        harness.setLibrary(player1, List.of(chosen));
        harness.setLibrary(player2, List.of());
        cast();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Scheming Symmetry");
    }

    @Test
    void bothEmptyLibrariesStillAllowSpellToFinish() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Scheming Symmetry");
    }

    private void cast() {
        harness.setHand(player1, List.of(new SchemingSymmetry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));
    }

    private void setupLibraries() {
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Plains(), new GrizzlyBears()));
    }
}
