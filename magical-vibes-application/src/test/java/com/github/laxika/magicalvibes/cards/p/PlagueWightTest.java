package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlagueWightTest extends BaseCardTest {

    @Test
    @DisplayName("When Plague Wight becomes blocked, each blocker gets -1/-1 until end of turn")
    void becomesBlockedShrinksEachBlocker() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        declareBlockers(wight, List.of(blocker));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(-1);
        assertThat(blocker.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Multiple blockers each get -1/-1")
    void multipleBlockersEachShrink() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new AirElemental());
        Permanent secondBlocker = addCreatureReady(player2, new AirElemental());

        declareBlockers(wight, List.of(firstBlocker, secondBlocker));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstBlocker.getPowerModifier()).isEqualTo(-1);
        assertThat(firstBlocker.getToughnessModifier()).isEqualTo(-1);
        assertThat(secondBlocker.getPowerModifier()).isEqualTo(-1);
        assertThat(secondBlocker.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent wight = addCreatureReady(player1, new PlagueWight());
        wight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        declareBlockers(wight, List.of(blocker));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    private void declareBlockers(Permanent wight, List<Permanent> blockers) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wight);
        gs.declareBlockers(gd, player2, blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
                .toList());
    }
}
