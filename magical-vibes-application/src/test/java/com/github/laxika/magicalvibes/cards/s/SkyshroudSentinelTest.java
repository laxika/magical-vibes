package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SkyshroudSentinel.class, SkyshroudRidgeback.class})
class SkyshroudSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Skyshroud Sentinel creates a may prompt")
    void resolvingCreatesMayPrompt() {
        castSentinel();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the may ability skips the search")
    void decliningMaySkipsSearch() {
        castSentinel();
        setupLibraryWithSentinels(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertOnBattlefield(player1, "Skyshroud Sentinel");
    }

    @Test
    @DisplayName("Accepting the may ability searches for up to three Skyshroud Sentinels")
    void acceptingMaySearchesForSentinels() {
        castSentinel();
        setupLibraryWithSentinels(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3)
                .allMatch(card -> card.getName().equals("Skyshroud Sentinel"));
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Accepting the may ability can stop before taking all three Sentinels")
    void acceptingMayCanStopBeforeMaximum() {
        castSentinel();
        setupLibraryWithSentinels(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Skyshroud Sentinel");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Skyshroud Sentinel", "Skyshroud Sentinel", "Skyshroud Ridgeback");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability completes without a prompt when no Sentinel is in the library")
    void acceptingMayWithNoMatchesCompletesSearch() {
        castSentinel();
        harness.setLibrary(player1, List.of(new SkyshroudRidgeback()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Skyshroud Ridgeback");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertOnBattlefield(player1, "Skyshroud Sentinel");
    }

    @Test
    @DisplayName("Chosen Skyshroud Sentinels are put into hand")
    void chosenSentinelsGoToHand() {
        castSentinel();
        setupLibraryWithSentinels(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Skyshroud Sentinel", "Skyshroud Sentinel", "Skyshroud Sentinel");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void castSentinel() {
        harness.setHand(player1, List.of(new SkyshroudSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveToMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupLibraryWithSentinels(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new SkyshroudSentinel());
        }
        deck.add(new SkyshroudRidgeback());
        harness.setLibrary(player1, deck);
    }
}
