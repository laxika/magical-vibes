package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbyssalHorror.class, GrizzlyBears.class, HillGiant.class})
class AbyssalHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target player discard two cards")
    void etbDiscardsTwo() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant()));
        castAbyssalHorror(player2.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB discards only the available card when target has fewer than two cards")
    void discardsWhatIsAvailable() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castAbyssalHorror(player2.getId());

        resolveAllTriggers();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanTargetItsController() {
        harness.setHand(player1, List.of(new AbyssalHorror(), new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player1.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Target player chooses exactly two cards from a larger hand")
    void targetChoosesTwoCardsFromLargerHand() {
        GrizzlyBears keptCard = new GrizzlyBears();
        HillGiant firstDiscard = new HillGiant();
        GrizzlyBears secondDiscard = new GrizzlyBears();
        harness.setHand(player2, List.of(keptCard, firstDiscard, secondDiscard));
        castAbyssalHorror(player2.getId());

        resolveAllTriggers();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Abyssal Horror");
    }

    private void castAbyssalHorror(UUID targetPlayerId) {
        castAbyssalHorror(targetPlayerId, List.of());
    }

    private void castAbyssalHorror(UUID targetPlayerId, List<Card> additionalHandCards) {
        List<Card> hand = new ArrayList<>();
        hand.add(new AbyssalHorror());
        hand.addAll(additionalHandCards);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB resolves without a discard when the target has no cards")
    void emptyHandDiscardsNothing() {
        harness.setHand(player2, List.of());
        castAbyssalHorror(player2.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB can target the creature's controller")
    void etbCanTargetController() {
        castAbyssalHorror(player1.getId(), List.of(new GrizzlyBears(), new HillGiant()));

        resolveAllTriggers();

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.playerId()).isEqualTo(player1.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
