package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgonizingRemorse.class, Forest.class, GrizzlyBears.class, Peek.class})
class AgonizingRemorseTest extends BaseCardTest {

    @Test
    void choosesANonlandFromHandAndExilesItThenYouLoseLife() {
        Card land = new Forest();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of(land, handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        int lifeBefore = gd.getLife(player1.getId());

        castAgonizingRemorse();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void canChooseANonlandFromTheTargetGraveyard() {
        Card land = new Forest();
        Card graveyardCard = new Peek();
        harness.setHand(player2, List.of(land));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castAgonizingRemorse();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotGrantPermissionToCastTheExiledCard() {
        Card exiledCard = new GrizzlyBears();
        harness.setHand(player2, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(exiledCard));

        castAgonizingRemorse();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiledCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new AgonizingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void castAgonizingRemorse() {
        harness.setHand(player1, List.of(new AgonizingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    void canChooseALandFromGraveyardInsteadOfANonlandFromHand() {
        Card handCard = new GrizzlyBears();
        Card graveyardLand = new Forest();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardLand));
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        castAgonizingRemorse();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardLand.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void mustExileTheOnlyGraveyardCardEvenWhenItIsALand() {
        Card graveyardLand = new Forest();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(graveyardLand));

        castAgonizingRemorse();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardLand.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void stillLosesLifeWhenOnlyLandsAreInHandAndGraveyardIsEmpty() {
        Card handLand = new Forest();
        harness.setHand(player2, List.of(handLand));
        harness.setGraveyard(player2, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        castAgonizingRemorse();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void cannotChooseALandFromHandOrDeclineAnAvailableNonland() {
        Card handLand = new Forest();
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, List.of(handLand, handCard));
        harness.setGraveyard(player2, List.of());

        castAgonizingRemorse();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(handLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handLand);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(handCard);
    }
}
