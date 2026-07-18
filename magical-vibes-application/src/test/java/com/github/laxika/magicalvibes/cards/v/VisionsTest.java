package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VisionsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving asks the controller whether to shuffle the target's library")
    void resolvingAsksControllerWhetherToShuffle() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining leaves the target's library untouched and in the same order")
    void decliningLeavesTargetLibraryUnchanged() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> before = new ArrayList<>(gd.playerDecks.get(player2.getId()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Pure look: no reorder, no shuffle — the whole library keeps its exact order.
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(before);
    }

    @Test
    @DisplayName("Accepting shuffles the target's library without removing any cards")
    void acceptingShufflesTargetLibrary() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> before = new ArrayList<>(gd.playerDecks.get(player2.getId()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // The shuffle only randomizes the library; no cards are drawn, exiled, or milled.
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrderElementsOf(before);
    }

    @Test
    @DisplayName("An empty target library offers no shuffle choice")
    void emptyTargetLibraryOffersNoChoice() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        gd.playerDecks.get(player2.getId()).clear();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Visions can look at the caster's own library")
    void canTargetOwnLibrary() {
        harness.setHand(player1, List.of(new Visions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
    }
}
