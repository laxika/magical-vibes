package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When blocked, accepting the trigger lets its controller look at the defending player's hand")
    void acceptingBlockedTriggerLooksAtDefendingPlayersHand() {
        declarePortInspectorBlocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    @Test
    @DisplayName("When blocked, declining the trigger does not reveal the defending player's hand")
    void decliningBlockedTriggerDoesNotLookAtHand() {
        declarePortInspectorBlocked();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    private void declarePortInspectorBlocked() {
        Permanent inspector = addCreatureReady(player1, new PortInspector());
        inspector.setAttacking(true);
        inspector.setAttackTarget(player2.getId());

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
