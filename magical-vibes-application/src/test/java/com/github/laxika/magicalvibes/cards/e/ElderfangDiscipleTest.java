package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderfangDisciple.class, GrizzlyBears.class})
class ElderfangDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes each opponent discard a card")
    void etbMakesOpponentDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castElderfangDisciple();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB trigger does nothing when the opponent has no cards in hand")
    void etbDoesNothingWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        castElderfangDisciple();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCard() {
        ElderfangDisciple kept = new ElderfangDisciple();
        ElderfangDisciple discarded = new ElderfangDisciple();
        ElderfangDisciple controllersCard = new ElderfangDisciple();
        harness.setHand(player2, List.of(kept, discarded));
        castElderfangDisciple();
        harness.setHand(player1, List.of(controllersCard));

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept, discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllersCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB trigger resolves even after Disciple dies")
    void triggerResolvesAfterSourceDies() {
        ElderfangDisciple discarded = new ElderfangDisciple();
        harness.setHand(player2, List.of(discarded));
        castElderfangDisciple();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).getFirst().setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Elderfang Disciple");
        harness.assertInGraveyard(player1, "Elderfang Disciple");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castElderfangDisciple() {
        harness.castFromHand(player1, new ElderfangDisciple(), "{1}{B}");
    }
}
