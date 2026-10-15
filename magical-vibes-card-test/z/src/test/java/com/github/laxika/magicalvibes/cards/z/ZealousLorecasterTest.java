package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.cards.i.ImpracticalJoke;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZealousLorecaster.class, EssenceScatter.class, ImpracticalJoke.class})
class ZealousLorecasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns the chosen instant or sorcery card to hand")
    void returnsChosenInstantOrSorceryToHand() {
        Card instant = new EssenceScatter();
        harness.setGraveyard(player1, List.of(instant, new ZealousLorecaster()));

        castZealousLorecaster();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Essence Scatter");
        harness.assertNotInGraveyard(player1, "Essence Scatter");
        harness.assertInGraveyard(player1, "Zealous Lorecaster");
    }

    @Test
    @DisplayName("Cannot decline the required graveyard target")
    void cannotDeclineRequiredTarget() {
        harness.setGraveyard(player1, List.of(new EssenceScatter()));

        castZealousLorecaster();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Essence Scatter");
    }

    @Test
    @DisplayName("Only instant and sorcery cards in your graveyard are valid choices")
    void excludesOtherCardsAndOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new ZealousLorecaster()));
        harness.setGraveyard(player2, List.of(new EssenceScatter()));

        castZealousLorecaster();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Zealous Lorecaster");
        harness.assertInGraveyard(player2, "Essence Scatter");
    }

    @Test
    @DisplayName("Returns one chosen sorcery and leaves the other eligible card in the graveyard")
    void returnsChosenSorceryOnly() {
        Card sorcery = new ImpracticalJoke();
        Card instant = new EssenceScatter();
        harness.setGraveyard(player1, List.of(sorcery, instant));

        castZealousLorecaster();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(sorcery.getId(), instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.assertNotInHand(player1, "Impractical Joke");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Impractical Joke");
        harness.assertNotInGraveyard(player1, "Impractical Joke");
        harness.assertInGraveyard(player1, "Essence Scatter");
        harness.assertNotInHand(player1, "Essence Scatter");
    }

    @Test
    @DisplayName("Does not choose a replacement when the graveyard target leaves before resolution")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new EssenceScatter();
        Card other = new ImpracticalJoke();
        harness.setGraveyard(player1, List.of(target, other));

        castZealousLorecaster();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Essence Scatter");
        harness.assertNotInHand(player1, "Impractical Joke");
        harness.assertInGraveyard(player1, "Impractical Joke");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Enters normally with an empty graveyard and no legal trigger target")
    void entersWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());

        castZealousLorecaster();

        harness.assertOnBattlefield(player1, "Zealous Lorecaster");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castZealousLorecaster() {
        harness.castFromHand(player1, new ZealousLorecaster(), "{5}{R}");
        harness.passBothPriorities();
    }
}
