package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtPartition.class, GrizzlyBears.class, Forest.class})
class ThoughtPartitionTest extends BaseCardTest {

    @Test
    void selectedNonlandCardStaysInHandAndPerpetuallyChangesCharacteristics() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new ThoughtPartition()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        Card modifiedBears = gd.playerHands.get(player2.getId()).getFirst();
        assertThat(modifiedBears).isNotSameAs(bears);
        assertThat(modifiedBears.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(modifiedBears.getColors()).containsExactly(CardColor.WHITE);
        assertThat(modifiedBears.getManaCost()).isEqualTo("{5}");

        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == modifiedBears);
    }

    @Test
    void mayDeclineWithoutChangingOrRemovingTheCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new ThoughtPartition()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyHandResolvesWithoutAChoice() {
        harness.setHand(player1, List.of(new ThoughtPartition()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void revealsOnlyNonlandCardsToBothPlayers() throws Exception {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ThoughtPartition()));
        harness.setHand(player2, List.of(forest, bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.zone() == GameEventFact.RevealZone.HAND
                        && reveal.subjectPlayerId().equals(player2.getId()))
                .isNotEmpty()
                .allSatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(bears.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }
}
