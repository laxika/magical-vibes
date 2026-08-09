package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Chosen Skyshroud Sentinels are put into hand")
    void chosenSentinelsGoToHand() {
        castSentinel();
        setupLibraryWithSentinels(3);

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < count; i++) {
            deck.add(new SkyshroudSentinel());
        }
        deck.add(new GrizzlyBears());
    }
}
