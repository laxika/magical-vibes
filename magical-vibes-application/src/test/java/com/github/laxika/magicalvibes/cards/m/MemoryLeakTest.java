package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryLeak.class, Divination.class, GrizzlyBears.class, Swamp.class})
class MemoryLeakTest extends BaseCardTest {

    @Test
    void choosesANonlandFromTheOpponentsHandOrGraveyard() {
        Card land = new Swamp();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Divination();
        harness.setHand(player2, List.of(land, handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castMemoryLeak();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                .hasMessageContaining("valid nonland card");

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, handCard);
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, graveyardCard.getId(), null, null))
                .hasMessageContaining("No permission to play this exiled card");
    }

    @Test
    void choosesANonlandFromTheOpponentsHand() {
        Card land = new Swamp();
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, List.of(land, handCard));

        castMemoryLeak();
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(handCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new MemoryLeak()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MemoryLeak()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Memory Leak");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void exilesFromGraveyardWhenTheOpponentsHandIsEmpty() {
        Card graveyardCard = new MemoryLeak();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(graveyardCard));

        castMemoryLeak();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Memory Leak");
    }

    @Test
    void resolvesWithoutAChoiceWhenBothZonesAreEmpty() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());

        castMemoryLeak();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Memory Leak");
    }

    @Test
    void doesNotExileLandsFromEitherZone() {
        Card handLand = new Swamp();
        Card graveyardLand = new Swamp();
        harness.setHand(player2, List.of(handLand));
        harness.setGraveyard(player2, List.of(graveyardLand));

        castMemoryLeak();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardLand);
        harness.assertInGraveyard(player1, "Memory Leak");
    }

    @Test
    void controllerMustChooseExactlyOneEligibleCard() {
        Card handCard = new MemoryLeak();
        Card graveyardCard = new MemoryLeak();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castMemoryLeak();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(handCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .hasMessageContaining("exactly one");
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(handCard.getId(), graveyardCard.getId())))
                .hasMessageContaining("exactly one");

        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    void cyclingDiscardsAsACostBeforeTheDrawResolves() {
        Card cycledCard = new MemoryLeak();
        Card drawnCard = new Swamp();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cycledCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void revealsTheEntireHandToBothPlayersBeforeChoosing() {
        Card land = new Swamp();
        Card nonland = new MemoryLeak();
        harness.setHand(player2, List.of(land, nonland));
        harness.clearMessages();

        castMemoryLeak();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("REVEAL_HAND") && message.contains(land.getId().toString())
                        && message.contains(nonland.getId().toString()));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("REVEAL_HAND") && message.contains(land.getId().toString())
                        && message.contains(nonland.getId().toString()));

        harness.handleMultipleCardsChosen(player1, List.of(nonland.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonland);
    }

    private void castMemoryLeak() {
        harness.setHand(player1, List.of(new MemoryLeak()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
