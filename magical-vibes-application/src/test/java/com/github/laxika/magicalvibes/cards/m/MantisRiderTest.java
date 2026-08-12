package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MantisRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Mantis Rider attack the turn it enters")
    void hasteAllowsAttackingWithSummoningSickness() {
        Permanent blocker = new Permanent(new MantisRider());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent rider = new Permanent(new MantisRider());
        gd.playerBattlefields.get(player1.getId()).add(rider);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(rider.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Mantis Rider untapped after attacking")
    void vigilancePreventsTappingWhenAttacking() {
        Permanent rider = new Permanent(new MantisRider());
        rider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(rider);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(rider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Mantis Rider")
    void flyingPreventsNonFlyingBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent rider = new Permanent(new MantisRider());
        rider.setSummoningSick(false);
        rider.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(rider);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int riderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rider);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, riderIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
