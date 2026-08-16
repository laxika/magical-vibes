package com.github.laxika.magicalvibes.cards.d;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

class DreamsOfSteelAndOilTest extends BaseCardTest {

    @Test
    void exilesAnArtifactOrCreatureFromHandThenGraveyard() {
        Card land = new Swamp();
        Card handCreature = new GrizzlyBears();
        Card invalidHandCard = new Divination();
        Card invalidGraveyardCard = new Divination();
        Card graveyardArtifact = new Ornithopter();
        harness.setHand(player2, List.of(land, handCreature, invalidHandCard));
        harness.setGraveyard(player2, List.of(invalidGraveyardCard, graveyardArtifact));

        castDreamsOfSteelAndOil();

        var handChoice = (PendingInteraction.RevealedHandChoice) gd.interaction.activeInteraction();
        assertThat(handChoice.validIndices()).containsExactly(1);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .hasMessageContaining("Invalid card index");

        harness.handleCardChosen(player1, 1);

        var graveyardChoice = (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(graveyardChoice.validCardIds()).containsExactly(graveyardArtifact.getId());
        assertThat(graveyardChoice.minCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(invalidGraveyardCard.getId())))
                .hasMessageContaining("Invalid card");

        harness.handleMultipleCardsChosen(player1, List.of(graveyardArtifact.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(handCreature.getId(), graveyardArtifact.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(land.getId(), invalidHandCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(invalidGraveyardCard.getId());
    }

    @Test
    void choosesFromGraveyardWhenHandHasNoArtifactOrCreature() {
        Card handCard = new Divination();
        Card graveyardArtifact = new Ornithopter();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardArtifact));

        castDreamsOfSteelAndOil();

        var graveyardChoice = (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(graveyardChoice.validCardIds()).containsExactly(graveyardArtifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardArtifact.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardArtifact.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(handCard.getId());
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.setHand(player1, List.of(new DreamsOfSteelAndOil()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void castDreamsOfSteelAndOil() {
        harness.setHand(player1, List.of(new DreamsOfSteelAndOil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
