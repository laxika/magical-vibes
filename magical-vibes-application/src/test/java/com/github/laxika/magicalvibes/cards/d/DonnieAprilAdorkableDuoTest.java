package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonnieAprilAdorkableDuo.class, Divination.class, Forest.class,
        LeoninScimitar.class, GrizzlyBears.class, Unsummon.class})
class DonnieAprilAdorkableDuoTest extends BaseCardTest {

    private static final String DRAW_MODE = "Target player draws two cards.";
    private static final String RETURN_MODE =
            "Target player returns an artifact, instant, or sorcery card from their graveyard to their hand.";

    @Test
    void bothModesTargetDifferentPlayersAndReturnOnlyAnEligibleCard() {
        Card drawnCard = new Forest();
        Card secondDrawnCard = new Forest();
        Card returnedCard = new Divination();
        Card otherEligibleCard = new LeoninScimitar();
        Card ineligibleCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard, secondDrawnCard));
        harness.setGraveyard(player2, List.of(returnedCard, otherEligibleCard, ineligibleCard));

        castDuo();
        harness.handleListChoice(player1, DRAW_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(returnedCard.getId(), otherEligibleCard.getId());

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnCard, secondDrawnCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(returnedCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(otherEligibleCard, ineligibleCard);
    }

    @Test
    void drawModeAloneDrawsTwoCardsForOpponent() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setHand(player2, List.of());

        castDuo();
        harness.handleListChoice(player1, DRAW_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
    }

    @Test
    void returnModeAloneReturnsAnInstantToControllerWithoutDrawing() {
        Card returnedCard = new Unsummon();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setLibrary(player1, List.of(libraryCard));

        castDuo();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returnedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void returnModeReturnsArtifactButLeavesCreatureAndLandInGraveyard() {
        Card artifact = new LeoninScimitar();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(artifact, creature, land));

        castDuo();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature, land);
    }

    @Test
    void bothModesCanTargetPlayerWithEmptyGraveyard() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());

        castDuo();
        harness.handleListChoice(player1, DRAW_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothModesMustTargetDifferentPlayers() {
        castDuo();
        harness.handleListChoice(player1, DRAW_MODE);
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
    }

    private void castDuo() {
        harness.setHand(player1, List.of(new DonnieAprilAdorkableDuo()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
