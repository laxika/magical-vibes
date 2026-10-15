package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Peek.class})
class PeekTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Peek puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast Peek without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Peek()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving Peek privately shows the entire opponent hand")
    void revealsOpponentHand() throws Exception {
        Card cardInHand1 = new Peek();
        Card cardInHand2 = new Peek();
        harness.setHand(player2, List.of(cardInHand1, cardInHand2));

        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<GameEventEnvelope> events = new ArrayList<>();
        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch ->
                batch.events().forEach(events::add))) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .singleElement().satisfies(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(cardInHand1.getId(), cardInHand2.getId());
                    assertThat(event.audience().playerIds()).containsExactly(player1.getId());
                });
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardInHand1, cardInHand2);
    }

    @Test
    @DisplayName("Resolving Peek against empty hand logs that hand is empty")
    void emptyHandLogged() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("looks at") && entry.plainText().contains("empty"));
    }

    @Test
    @DisplayName("Can target self to look at own hand")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("looks at") && entry.plainText().contains("hand"));
    }

    @Test
    @DisplayName("Resolving Peek draws a card")
    void drawsACard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Hand should have 1 card (Peek left hand, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Peek goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Peek");
    }

    @Test
    @DisplayName("Looking at an empty hand still draws for the spell controller")
    void emptyHandStillDrawsForOtherController() {
        Peek drawnCard = new Peek();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Peek()));
        harness.setLibrary(player2, List.of(drawnCard, new Peek()));
        int opponentLibrarySize = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(opponentLibrarySize);
        harness.assertInGraveyard(player2, "Peek");
    }
}
