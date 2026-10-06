package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessNegotiation.class, Forest.class, DaggerfangDuo.class})
class RuthlessNegotiationTest extends BaseCardTest {

    @Test
    void exilesAChosenCardFromTargetOpponentsHand() {
        Card chosen = new DaggerfangDuo();
        Card remaining = new Forest();
        harness.setHand(player2, List.of(chosen, remaining));
        harness.setHand(player1, List.of(new RuthlessNegotiation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void flashbackExilesAChosenCardAndDrawsACard() {
        Card chosen = new DaggerfangDuo();
        Card remaining = new Forest();
        Card draw = new Forest();
        harness.setHand(player2, List.of(chosen, remaining));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new RuthlessNegotiation()));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ruthless Negotiation"));
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new RuthlessNegotiation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void doesNotRevealOpponentsHand() throws Exception {
        harness.setHand(player2, List.of(new DaggerfangDuo(), new Forest()));
        harness.setHand(player1, List.of(new RuthlessNegotiation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.castAndResolveSorcery(player1, 0, player2.getId());
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.zone() == GameEventFact.RevealZone.HAND
                        && reveal.subjectPlayerId().equals(player2.getId())
                        && event.audience().playerIds().contains(player1.getId()))
                .isEmpty();
        harness.handleCardChosen(player2, 1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    void castFromHandWithEmptyOpponentHandDoesNotDraw() {
        Card spell = new RuthlessNegotiation();
        Card draw = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void flashbackDrawsEvenWhenOpponentHasNoCards() {
        Card spell = new RuthlessNegotiation();
        Card draw = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
