package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderworldSentinel.class, GrizzlyBears.class, CruelEdict.class})
class UnderworldSentinelTest extends BaseCardTest {

    @Test
    void attackExilesTargetCreatureFromControllerGraveyard() {
        Permanent sentinel = addCreatureReady(player1, new UnderworldSentinel());
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears opponentBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(opponentBears));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(sentinel.getId())).containsExactly(bears);
    }

    @Test
    void deathReturnsAllCardsExiledWithSentinel() {
        Permanent sentinel = addCreatureReady(player1, new UnderworldSentinel());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(sentinel.getId())).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertInGraveyard(player1, "Underworld Sentinel");
    }
}
