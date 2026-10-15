package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SkyshroudClaim;
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

@CardUsed({NestingWurm.class, SkyshroudClaim.class})
class NestingWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may prompt")
    void enteringBattlefieldCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting searches for up to three revealed Nesting Wurms")
    void acceptingSearchesForUpToThreeNestingWurms() {
        setupAndCast();
        setupLibraryWithNestingWurms(4);

        resolveMayPrompt(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Nesting Wurm"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting may can find fewer than three Nesting Wurms")
    void acceptingMayCanFindFewerThanThreeNestingWurms() {
        setupAndCast();
        setupLibraryWithNestingWurms(4);

        resolveMayPrompt(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining does not search the library")
    void decliningSkipsSearch() {
        setupAndCast();
        setupLibraryWithNestingWurms(2);

        resolveMayPrompt(false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An accepted search may find zero cards even when matches exist")
    void acceptedSearchCanFindZeroCards() {
        setupAndCast();
        setupLibraryWithNestingWurms(2);
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        resolveMayPrompt(true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
    }

    @Test
    @DisplayName("Search finds only Nesting Wurms in the controller's library and ends when matches run out")
    void searchFiltersCardsAndEndsWhenMatchesRunOut() {
        setupAndCast();
        NestingWurm first = new NestingWurm();
        NestingWurm second = new NestingWurm();
        SkyshroudClaim other = new SkyshroudClaim();
        NestingWurm opponentsWurm = new NestingWurm();
        harness.setLibrary(player1, List.of(other, first, second));
        harness.setLibrary(player2, List.of(opponentsWurm));
        harness.setHand(player2, List.of());

        resolveMayPrompt(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(first, second);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsWurm);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An accepted search with no matching cards finishes without moving other cards")
    void searchWithNoMatchesFinishes() {
        setupAndCast();
        SkyshroudClaim other = new SkyshroudClaim();
        harness.setLibrary(player1, List.of(other));

        resolveMayPrompt(true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("An accepted search of an empty library finishes normally")
    void searchOfEmptyLibraryFinishes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveMayPrompt(true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new NestingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithNestingWurms(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new NestingWurm());
        }
        harness.setLibrary(player1, deck);
    }

    private void resolveMayPrompt(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
