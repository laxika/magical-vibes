package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.RandomDiscardCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CaseyJonesVigilante.class)
class CaseyJonesVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws three cards and schedules a random discard")
    void enteringDrawsAndSchedulesRandomDiscard() {
        castCasey();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getDelayedActions(RandomDiscardCardsAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.getDelayedActions(RandomDiscardCardsAtNextUpkeep.class).getFirst().controllerId())
                .isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(RandomDiscardCardsAtNextUpkeep.class).getFirst().count()).isEqualTo(3);
    }

    @Test
    @DisplayName("The random discard waits for the controller's next upkeep")
    void randomDiscardResolvesAtControllersNextUpkeep() {
        castCasey();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(RandomDiscardCardsAtNextUpkeep.class)).hasSize(1);

        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        assertThat(gd.getDelayedActions(RandomDiscardCardsAtNextUpkeep.class)).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castCasey() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CaseyJonesVigilante()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
