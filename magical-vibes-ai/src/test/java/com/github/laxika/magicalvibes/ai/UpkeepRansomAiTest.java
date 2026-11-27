package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.p.PhantasmalForces;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** How the hard AI answers an upkeep "sacrifice this unless you pay" of its own permanent. */
@DisplayName("Hard AI answers to an upkeep ransom")
class UpkeepRansomAiTest extends HardAiDecisionEngineTestSupport {

    @Test
    @DisplayName("Phantasmal Forces: the hard AI pays {U} to keep its creature")
    void hardAiPaysTheUpkeepToKeepItsSource() {
        HardAiDecisionEngine ai = createHardAi(player2);
        harness.addToBattlefield(player2, new PhantasmalForces());
        givePlayerIslands(player2, 1);
        harness.setHand(player2, List.of());

        if (!player2.getId().equals(gd.activePlayerId)) {
            harness.beginLaterTurn();
        }
        harness.performUntapStep(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();
        gd.status = GameStatus.RUNNING;
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        for (int answers = 0; answers < 3
                && gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null; answers++) {
            ai.handleEvent(AiDecisionKind.INTERACTION);
        }

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Phantasmal Forces"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Phantasmal Forces"));
    }
}
