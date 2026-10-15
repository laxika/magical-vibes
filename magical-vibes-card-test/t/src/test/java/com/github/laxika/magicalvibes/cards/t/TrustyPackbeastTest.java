package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.f.FountainOfRenewal;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrustyPackbeast.class, FountainOfRenewal.class, GreenwoodSentinel.class})
class TrustyPackbeastTest extends BaseCardTest {

    /** Casts Trusty Packbeast and resolves it so its ETB trigger sets up graveyard targeting. */
    private void castPackbeast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TrustyPackbeast(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted artifact card from the graveyard to hand")
    void etbReturnsArtifactToHand() {
        FountainOfRenewal fountain = new FountainOfRenewal();
        harness.setGraveyard(player1, List.of(fountain));

        castPackbeast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(fountain.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Fountain of Renewal");
        harness.assertInHand(player1, "Fountain of Renewal");
    }

    @Test
    @DisplayName("A non-artifact card in the graveyard is not a legal target")
    void nonArtifactNotTargetable() {
        harness.setGraveyard(player1, List.of(new GreenwoodSentinel()));

        castPackbeast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Only the chosen artifact returns when multiple artifacts are available")
    void returnsOnlyChosenArtifact() {
        FountainOfRenewal chosen = new FountainOfRenewal();
        FountainOfRenewal other = new FountainOfRenewal();
        harness.setGraveyard(player1, List.of(chosen, other));

        castPackbeast();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("An artifact in an opponent's graveyard cannot be targeted")
    void opponentArtifactNotTargetable() {
        harness.setGraveyard(player2, List.of(new FountainOfRenewal()));

        castPackbeast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Fountain of Renewal");
        harness.assertNotInHand(player1, "Fountain of Renewal");
        harness.assertOnBattlefield(player1, "Trusty Packbeast");
    }

    @Test
    @DisplayName("The ability does not return another artifact when its target leaves the graveyard")
    void missingTargetDoesNotReturnAnotherArtifact() {
        FountainOfRenewal chosen = new FountainOfRenewal();
        FountainOfRenewal other = new FountainOfRenewal();
        harness.setGraveyard(player1, List.of(chosen, other));

        castPackbeast();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(chosen));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Fountain of Renewal");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }
}
