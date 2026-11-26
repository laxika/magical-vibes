package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TormentOfScarabs;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** How the hard AI answers an "unless" whose ransom is asked one alternative at a time. */
@DisplayName("Hard AI answers to an unless ransom with alternatives")
class UnlessAlternativeRansomAiTest extends HardAiDecisionEngineTestSupport {

    @Test
    @DisplayName("Torment of Scarabs: the hard AI keeps its Serra Angel and discards a card instead")
    void hardAiPricesTheAlternativeItIsAsked() {
        HardAiDecisionEngine ai = createHardAi(player2);
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new TormentOfScarabs());
        curse.setAttachedTo(player2.getId());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        if (!player2.getId().equals(gd.activePlayerId)) {
            harness.beginLaterTurn();
        }
        harness.performUntapStep(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();
        gd.status = GameStatus.RUNNING;
        for (int answers = 0; answers < 5
                && (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null
                || gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null); answers++) {
            ai.handleEvent(AiDecisionKind.INTERACTION);
        }

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
