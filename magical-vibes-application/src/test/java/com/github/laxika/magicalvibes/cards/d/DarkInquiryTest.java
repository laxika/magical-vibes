package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HardyVeteran;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkInquiry.class, Forest.class, HardyVeteran.class})
class DarkInquiryTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses and discards a nonland card from the target opponent's hand")
    void choosesAndDiscardsNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new HardyVeteran(), new Forest())));
        harness.setHand(player1, List.of(new DarkInquiry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Hardy Veteran");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot choose a land from the revealed hand")
    void cannotChooseLand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new HardyVeteran())));
        harness.setHand(player1, List.of(new DarkInquiry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("A hand containing only lands results in no discard")
    void onlyLandsResultsInNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, List.of(new DarkInquiry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target an opponent but not the caster")
    void targetsOpponentOnly() {
        harness.setHand(player1, List.of(new DarkInquiry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty hand resolves without a choice or discard")
    void emptyHandResolvesWithoutDiscard() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DarkInquiry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dark Inquiry");
    }

    @Test
    @DisplayName("The caster chooses exactly one of multiple nonland cards")
    void choosesExactlyOneOfMultipleNonlandCards() {
        DarkInquiry chosen = new DarkInquiry();
        HardyVeteran remaining = new HardyVeteran();
        Forest land = new Forest();
        harness.setHand(player2, List.of(remaining, land, chosen));
        harness.setHand(player1, List.of(new DarkInquiry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 2);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 2))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dark Inquiry");
    }
}
