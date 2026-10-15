package com.github.laxika.magicalvibes.cards.d;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.UnionOfTheThirdPath;
import com.github.laxika.magicalvibes.cards.v.VeteransPowerblade;
import com.github.laxika.magicalvibes.cards.y.YotianMedic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({DreamsOfSteelAndOil.class, Swamp.class, YotianMedic.class,
        VeteransPowerblade.class, UnionOfTheThirdPath.class})
class DreamsOfSteelAndOilTest extends BaseCardTest {

    @Test
    void exilesAnArtifactOrCreatureFromHandThenGraveyard() {
        Card land = new Swamp();
        Card handCreature = new YotianMedic();
        Card invalidHandCard = new UnionOfTheThirdPath();
        Card invalidGraveyardCard = new UnionOfTheThirdPath();
        Card graveyardArtifact = new VeteransPowerblade();
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
        Card handCard = new UnionOfTheThirdPath();
        Card graveyardArtifact = new VeteransPowerblade();
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
        harness.setHand(player2, List.of(new VeteransPowerblade()));
        harness.setHand(player1, List.of(new DreamsOfSteelAndOil()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    @Test
    void waitsUntilBothCardsAreChosenBeforeExilingEither() {
        Card handArtifact = new VeteransPowerblade();
        Card graveyardCreature = new YotianMedic();
        harness.setHand(player2, List.of(handArtifact));
        harness.setGraveyard(player2, List.of(graveyardCreature));

        castDreamsOfSteelAndOil();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCreature);

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(handArtifact, graveyardCreature);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void exilesHandArtifactWhenGraveyardHasNoMatchingCard() {
        Card handArtifact = new VeteransPowerblade();
        Card graveyardInstant = new UnionOfTheThirdPath();
        harness.setHand(player2, List.of(handArtifact));
        harness.setGraveyard(player2, List.of(graveyardInstant));

        castDreamsOfSteelAndOil();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handArtifact);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardInstant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesGraveyardCreatureWhenHandIsEmpty() {
        Card graveyardCreature = new YotianMedic();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(graveyardCreature));

        castDreamsOfSteelAndOil();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesExactlyOneCardFromEachZoneWhenSeveralQualify() {
        Card handArtifact = new VeteransPowerblade();
        Card handCreature = new YotianMedic();
        Card graveyardArtifact = new VeteransPowerblade();
        Card graveyardCreature = new YotianMedic();
        harness.setHand(player2, List.of(handArtifact, handCreature));
        harness.setGraveyard(player2, List.of(graveyardArtifact, graveyardCreature));

        castDreamsOfSteelAndOil();
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(handArtifact, graveyardCreature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardArtifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutChoicesWhenBothZonesAreEmpty() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());

        castDreamsOfSteelAndOil();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dreams of Steel and Oil");
    }

    private void castDreamsOfSteelAndOil() {
        harness.setHand(player1, List.of(new DreamsOfSteelAndOil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
