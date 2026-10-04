package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishRegrower.class, GrizzlyBears.class, HolyDay.class, Forest.class})
class ElvishRegrowerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted permanent card from the graveyard to hand")
    void etbReturnsPermanentCardToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        HolyDay holyDay = new HolyDay();
        harness.setGraveyard(player1, List.of(bears, holyDay));

        castElvishRegrower();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("ETB does not allow declining when a legal permanent target exists")
    void targetIsMandatory() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castElvishRegrower();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 1 cards");

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nonpermanent cards are not legal targets")
    void nonPermanentIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new HolyDay()));

        castElvishRegrower();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("A permanent card in an opponent's graveyard is not a legal target")
    void opponentGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castElvishRegrower();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can return a land card to hand")
    void returnsLandCardToHand() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castElvishRegrower();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("An empty graveyard does not prevent the creature from entering")
    void entersWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());

        castElvishRegrower();

        harness.assertOnBattlefield(player1, "Elvish Regrower");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not returned and cannot be replaced")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Forest target = new Forest();
        Forest other = new Forest();
        harness.setGraveyard(player1, List.of(target, other));

        castElvishRegrower();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Elvish Regrower");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castElvishRegrower() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ElvishRegrower()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
