package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeverHappened.class, Forest.class, GrizzlyBears.class, Peek.class})
class NeverHappenedTest extends BaseCardTest {

    @Test
    void choosesANonlandFromHandOrGraveyardAndExilesIt() {
        Card land = new Forest();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(land, handCard)));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castNeverHappened();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, handCard);
    }

    @Test
    void doesNotGrantPermissionToCastTheExiledCard() {
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(exiledCard));

        castNeverHappened();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiledCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void revealsTheEntireHandIncludingLandsToBothPlayers() throws Exception {
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setHand(player2, List.of(land, nonland));
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            castNeverHappened();
            harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal)
                .anySatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.subjectPlayerId()).isEqualTo(player2.getId());
                    assertThat(reveal.zone()).isEqualTo(GameEventFact.RevealZone.HAND);
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(land.getId(), nonland.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @Test
    void exilesChosenHandCardAndLeavesGraveyardUntouched() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castNeverHappened();
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
    }

    @Test
    void requiresCasterToChooseExactlyOneNonlandAndRejectsLandsInEitherZone() {
        Card handLand = new Forest();
        Card graveyardLand = new Forest();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of(handLand, handCard));
        harness.setGraveyard(player2, List.of(graveyardLand, graveyardCard));

        castNeverHappened();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(handCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        for (List<UUID> choice : List.of(
                List.<UUID>of(), List.of(handLand.getId()), List.of(graveyardLand.getId()),
                List.of(handCard.getId(), graveyardCard.getId()))) {
            assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, choice))
                    .isInstanceOf(IllegalStateException.class);
        }
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand, handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    void canChooseFromGraveyardWithAnEmptyHand() {
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(graveyardCard));

        castNeverHappened();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    void resolvesWithoutAChoiceWhenBothZonesContainOnlyLands() {
        Card handLand = new Forest();
        Card graveyardLand = new Forest();
        harness.setHand(player2, List.of(handLand));
        harness.setGraveyard(player2, List.of(graveyardLand));

        castNeverHappened();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new NeverHappened()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void castNeverHappened() {
        harness.setHand(player1, List.of(new NeverHappened()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
