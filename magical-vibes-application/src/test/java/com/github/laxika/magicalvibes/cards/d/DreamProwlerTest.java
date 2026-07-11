package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Dream Prowler can't be blocked while attacking alone")
    void cantBeBlockedWhenAttackingAlone() {
        // Defender has a creature that could block
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Dream Prowler is the only attacker
        Permanent prowler = new Permanent(new DreamProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(prowler);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Dream Prowler can be blocked when attacking alongside another creature")
    void canBeBlockedWhenNotAttackingAlone() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Dream Prowler (index 0) attacks alongside a second attacker (index 1)
        Permanent prowler = new Permanent(new DreamProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(prowler);

        Permanent companion = new Permanent(new GrizzlyBears());
        companion.setSummoningSick(false);
        companion.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(companion);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Blocking Dream Prowler is now legal — it's no longer attacking alone
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Unblocked Dream Prowler deals combat damage when attacking alone")
    void dealsDamageWhenUnblockedAlone() {
        harness.setLife(player2, 20);

        Permanent prowler = new Permanent(new DreamProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(prowler);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Dream Prowler is a 1/5
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
