package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranEscort.class, ElvishRanger.class})
class KjeldoranEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Banding makes a blocker block the whole attacking band")
    void bandingSharesBlockersAcrossTheBand() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new KjeldoranEscort());
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
        escort.setSummoningSick(false);
        ranger.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.getBlockingTargetIds()).contains(escort.getId(), ranger.getId());
    }
}
