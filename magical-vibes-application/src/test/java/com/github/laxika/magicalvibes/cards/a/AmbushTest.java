package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeapingLizard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ambush.class, LeapingLizard.class})
class AmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Ambush grants first strike to blocking creatures only")
    void grantsFirstStrikeToBlockers() {
        Permanent attacker = addCreatureReady(player1, new LeapingLizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LeapingLizard());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        Permanent idle = addCreatureReady(player2, new LeapingLizard());

        castAmbush();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, idle, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ambush grants first strike to a blocker declared through combat")
    void grantsFirstStrikeToDeclaredBlocker() {
        Permanent attacker = addCreatureReady(player1, new LeapingLizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LeapingLizard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        castAmbush();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent attacker = addCreatureReady(player1, new LeapingLizard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new LeapingLizard());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        castAmbush();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void castAmbush() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Ambush(), "{3}{R}");
        harness.passBothPriorities();
    }
}
