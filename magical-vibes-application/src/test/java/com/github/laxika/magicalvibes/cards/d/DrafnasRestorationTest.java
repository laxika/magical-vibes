package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
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

        harness.castSorcery(player1, 0, 0);

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

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);

        List<UUID> targets = List.of(ownArtifact.getId(), opponentArtifact.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, targets))
                .isInstanceOf(IllegalStateException.class);
    }
}
