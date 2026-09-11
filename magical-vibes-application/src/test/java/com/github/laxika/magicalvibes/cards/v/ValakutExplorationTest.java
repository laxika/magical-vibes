package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValakutExploration.class, Forest.class, GrizzlyBears.class})
class ValakutExplorationTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall exiles the top card of the controller's library")
    void landfallExilesTopCard() {
        Permanent exploration = addExploration();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("At the controller's end step, remaining exiled cards go to their owners' graveyards and damage each opponent")
    void endStepReturnsCardsAndDealsDamage() {
        Permanent exploration = addExploration();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        triggerControllerEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(exploration.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("The play permission remains after Valakut Exploration leaves the battlefield")
    void playPermissionRemainsAfterSourceLeaves() {
        Permanent exploration = addExploration();
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(exploration);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The end-step ability does not trigger without cards exiled with Valakut Exploration")
    void endStepDoesNotTriggerWithoutExiledCards() {
        addExploration();

        triggerControllerEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addExploration() {
        return harness.addToBattlefieldAndReturn(player1, new ValakutExploration());
    }

    private void triggerControllerEndStep(Player player) {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
    }
}
