package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeadGolemTest extends BaseCardTest {

    // ===== Attack trigger pushes onto stack =====

    @Test
    @DisplayName("Attacking with Lead Golem pushes a triggered ability sourced from itself")
    void attackTriggerPushesOntoStack() {
        Permanent golem = addReadyGolem(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(golem.getId());
    }

    // ===== Attack trigger resolution =====

    @Test
    @DisplayName("Resolving the attack trigger sets skipUntapCount on Lead Golem itself")
    void resolvingSetsSkipUntapCountOnSelf() {
        Permanent golem = addReadyGolem(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(golem.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving the trigger logs that Lead Golem won't untap")
    void resolvingLogsSkipUntap() {
        addReadyGolem(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Lead Golem") && log.contains("untap"));
    }

    // ===== Helpers =====

    private Permanent addReadyGolem(Player player) {
        Permanent perm = new Permanent(new LeadGolem());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
