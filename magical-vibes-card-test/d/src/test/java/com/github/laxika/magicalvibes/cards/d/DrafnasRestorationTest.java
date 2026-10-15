package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrafnasRestoration.class, FountainOfYouth.class, GrizzlyBears.class, LeoninScimitar.class})
class DrafnasRestorationTest extends BaseCardTest {

    @Test
    @DisplayName("Can target artifact cards from the controller's graveyard")
    void canTargetOwnGraveyard() {
        Card artifact = new LeoninScimitar();
        Card nonArtifact = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, nonArtifact));
        harness.setGraveyard(player2, List.of(new FountainOfYouth()));
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(artifact.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonArtifact);
    }

    @Test
    @DisplayName("Can target multiple artifact cards from one opponent's graveyard")
    void canTargetOpponentGraveyard() {
        Card first = new LeoninScimitar();
        Card second = new FountainOfYouth();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder = gd.interaction
                .activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(second), reorder.cards().indexOf(first))));

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2)).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("All chosen cards must come from one player's graveyard")
    void rejectsTargetsFromDifferentGraveyards() {
        Card ownArtifact = new LeoninScimitar();
        Card opponentArtifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(ownArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());

        List<UUID> targets = List.of(ownArtifact.getId(), opponentArtifact.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster chooses their own library order during resolution")
    void choosesOwnLibraryOrderDuringResolution() {
        Card first = new LeoninScimitar();
        Card second = new FountainOfYouth();
        Card existingTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(existingTop));
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder = gd.interaction
                .activeInteraction(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(first), reorder.cards().indexOf(second))));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, existingTop);
    }

    @Test
    @DisplayName("Zero artifact targets still requires a target player")
    void zeroArtifactTargetsStillTargetsPlayer() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Drafna's Restoration");
    }

    @Test
    @DisplayName("The caster may choose no artifacts")
    void canChooseNoArtifacts() {
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Drafna's Restoration");
    }

    @Test
    @DisplayName("Only artifact targets still in the graveyard are returned")
    void returnsOnlyRemainingLegalArtifact() {
        Card removed = new LeoninScimitar();
        Card remaining = new FountainOfYouth();
        harness.setGraveyard(player2, List.of(removed, remaining));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new DrafnasRestoration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removed);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
