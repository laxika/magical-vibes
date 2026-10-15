package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Encroach.class, Forest.class, YavimayaHollow.class, WallOfGlare.class})
class EncroachTest extends BaseCardTest {

    @Test
    @DisplayName("Only a nonbasic land card can be chosen from the revealed hand")
    void onlyNonbasicLandCanBeChosen() {
        castEncroachAt(List.of(new Forest(), new YavimayaHollow(), new WallOfGlare()));

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Yavimaya Hollow");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Wall of Glare");
    }

    @Test
    @DisplayName("A basic land cannot be chosen")
    void basicLandCannotBeChosen() {
        castEncroachAt(List.of(new Forest()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("A non-land card cannot be chosen")
    void nonLandCannotBeChosen() {
        castEncroachAt(List.of(new WallOfGlare(), new YavimayaHollow()));

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("The spell can target its controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new Encroach(), new YavimayaHollow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Yavimaya Hollow");
    }

    @Test
    @DisplayName("An empty hand resolves without a card choice")
    void emptyHandResolvesWithoutChoice() {
        castEncroachAt(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Encroach");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A hand containing only nonlands is revealed without discarding")
    void nonlandOnlyHandDoesNotDiscard() {
        castEncroachAt(List.of(new WallOfGlare()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Wall of Glare");
        harness.assertNotInGraveyard(player2, "Wall of Glare");
        harness.assertInGraveyard(player1, "Encroach");
    }

    @Test
    @DisplayName("The caster chooses exactly one of multiple eligible land cards")
    void choosesExactlyOneNonbasicLand() {
        YavimayaHollow first = new YavimayaHollow();
        YavimayaHollow second = new YavimayaHollow();
        castEncroachAt(List.of(first, second));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Encroach");
    }

    private void castEncroachAt(List<com.github.laxika.magicalvibes.model.Card> targetHand) {
        harness.setHand(player2, targetHand);
        harness.setHand(player1, List.of(new Encroach()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
