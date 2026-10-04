package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.o.OvergrownTomb;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgariFindbroker.class, BartizanBats.class, DirectCurrent.class, OvergrownTomb.class})
class GolgariFindbrokerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted permanent card from the graveyard to hand")
    void etbReturnsPermanentCardToHand() {
        BartizanBats bats = new BartizanBats();
        DirectCurrent directCurrent = new DirectCurrent();
        harness.setGraveyard(player1, List.of(bats, directCurrent));

        castGolgariFindbroker();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bats.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bats.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bartizan Bats");
        harness.assertInGraveyard(player1, "Direct Current");
    }

    @Test
    @DisplayName("ETB does not allow declining when a legal permanent target exists")
    void targetIsMandatory() {
        BartizanBats bats = new BartizanBats();
        harness.setGraveyard(player1, List.of(bats));

        castGolgariFindbroker();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 1 cards");

        harness.handleMultipleCardsChosen(player1, List.of(bats.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bartizan Bats");
    }

    @Test
    @DisplayName("Nonpermanent cards are not legal targets")
    void nonPermanentIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new DirectCurrent()));

        castGolgariFindbroker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Direct Current");
    }

    @Test
    @DisplayName("A permanent card in an opponent's graveyard is not a legal target")
    void opponentGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new BartizanBats()));

        castGolgariFindbroker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Bartizan Bats");
    }

    @Test
    @DisplayName("ETB can return a land card and leaves other permanents in the graveyard")
    void returnsLandCard() {
        OvergrownTomb land = new OvergrownTomb();
        BartizanBats bats = new BartizanBats();
        harness.setGraveyard(player1, List.of(land, bats));

        castGolgariFindbroker();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(land.getId(), bats.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Overgrown Tomb");
        harness.assertNotInGraveyard(player1, "Overgrown Tomb");
        harness.assertInGraveyard(player1, "Bartizan Bats");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void removedTargetIsNotReplaced() {
        BartizanBats bats = new BartizanBats();
        OvergrownTomb land = new OvergrownTomb();
        harness.setGraveyard(player1, List.of(bats, land));

        castGolgariFindbroker();
        harness.handleMultipleCardsChosen(player1, List.of(bats.getId()));
        harness.setGraveyard(player1, List.of(land));
        harness.setExile(player1, List.of(bats));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Bartizan Bats");
        harness.assertNotInHand(player1, "Overgrown Tomb");
        harness.assertInGraveyard(player1, "Overgrown Tomb");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bats);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void castGolgariFindbroker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GolgariFindbroker(), "{B}{B}{G}{G}");
        harness.passBothPriorities();
    }
}
