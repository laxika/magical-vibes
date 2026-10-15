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
        addCreatureReady(player1, new LeapingLizard());
        Permanent blocker = addCreatureReady(player2, new LeapingLizard());

        declareAttackersAndPrepareBlockers(List.of(0));
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

    @Test
    @DisplayName("Casting Ambush before blocks does not grant first strike to later blockers")
    void doesNotAffectLaterBlockers() {
        addCreatureReady(player1, new LeapingLizard());
        Permanent blocker = addCreatureReady(player2, new LeapingLizard());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Ambush(), "{3}{R}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A defending player's Ambush affects every blocker and lasts beyond combat")
    void defenderCanGrantFirstStrikeToMultipleBlockers() {
        addCreatureReady(player1, new LeapingLizard());
        addCreatureReady(player1, new LeapingLizard());
        Permanent firstBlocker = addCreatureReady(player2, new LeapingLizard());
        Permanent secondBlocker = addCreatureReady(player2, new LeapingLizard());
        Permanent idle = addCreatureReady(player2, new LeapingLizard());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        harness.castFromHand(player2, new Ambush(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstBlocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBlocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, idle, Keyword.FIRST_STRIKE)).isFalse();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(firstBlocker.isBlocking()).isFalse();
        assertThat(secondBlocker.isBlocking()).isFalse();
        assertThat(gqs.hasKeyword(gd, firstBlocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBlocker, Keyword.FIRST_STRIKE)).isTrue();
    }

    private void castAmbush() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Ambush(), "{3}{R}");
        harness.passBothPriorities();
    }
}
