package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DispossessTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting the opponent")
    void castingTargetsOpponent() {
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    // ===== Name choice filtering =====

    @Test
    @DisplayName("Only artifact card names are offered")
    void offersOnlyArtifactNames() {
        harness.setHand(player2, new ArrayList<>(List.of(new Ornithopter(), new GrizzlyBears())));

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Ornithopter");
        assertThat(choice.options()).doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("After name choice with matches, prompts for card selection")
    void afterNameChoicePromptsForSelection() {
        harness.setHand(player2, new ArrayList<>(List.of(new Ornithopter())));

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Ornithopter");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
    }

    // ===== Exile behavior =====

    @Test
    @DisplayName("Exiles matching artifacts from the opponent's hand, graveyard, and library")
    void exilesMatchingArtifactsFromAllZones() {
        Card thopter1 = new Ornithopter();
        Card thopter2 = new Ornithopter();
        Card thopter3 = new Ornithopter();

        harness.setHand(player2, new ArrayList<>(List.of(thopter1)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(thopter2)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(thopter3);

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Ornithopter");
        harness.handleMultipleCardsChosen(player1, List.of(thopter1.getId(), thopter2.getId(), thopter3.getId()));

        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Ornithopter"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c.getName().equals("Ornithopter"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Ornithopter"));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Choosing a name with no matches just shuffles the library")
    void noMatchesShufflesLibrary() {
        harness.setHand(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Ornithopter");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 0 cards"));
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Dispossess goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Dispossess()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Ornithopter");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dispossess"));
    }
}
