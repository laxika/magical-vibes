package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearchWarrant.class, AxebaneStag.class})
class SearchWarrantTest extends BaseCardTest {

    private void castSearchWarrant(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SearchWarrant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Controller gains life equal to target player's hand size")
    void gainsLifeEqualToTargetHandSize() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new AxebaneStag(), new AxebaneStag(), new AxebaneStag()));

        castSearchWarrant(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Search Warrant");
    }

    @Test
    @DisplayName("Uses target hand size on resolution")
    void usesHandSizeOnResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new AxebaneStag(), new AxebaneStag()));

        harness.setHand(player1, List.of(new SearchWarrant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, player2.getId());
        gd.playerHands.get(player2.getId()).add(new AxebaneStag());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Gains 0 life if target hand is empty")
    void emptyHandGainsZero() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of());

        castSearchWarrant(player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SearchWarrant(), new AxebaneStag(), new AxebaneStag()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // After casting, hand still has two other cards.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Reveals the entire target hand to both players without removing cards")
    void revealsHandToBothPlayers() throws Exception {
        AxebaneStag first = new AxebaneStag();
        SearchWarrant second = new SearchWarrant();
        harness.setHand(player2, List.of(first, second));
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            castSearchWarrant(player2.getId());
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .singleElement().satisfies(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(first.getId(), second.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Targeting self with no other cards gains no life")
    void spellOnStackDoesNotCountAsACardInHand() {
        harness.setLife(player1, 20);

        castSearchWarrant(player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Search Warrant");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AxebaneStag());

        harness.setHand(player1, List.of(new SearchWarrant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
