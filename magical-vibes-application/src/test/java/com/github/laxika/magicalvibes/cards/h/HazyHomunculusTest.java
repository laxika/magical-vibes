package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HazyHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Hazy Homunculus can't be blocked when defending player controls an untapped land")
    void cannotBeBlockedWhenDefenderControlsUntappedLand() {
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addReadyBlocker();
        Permanent homunculus = addAttackingHomunculus();
        beginBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(homunculus)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Hazy Homunculus can be blocked when defending player controls only tapped lands")
    void canBeBlockedWhenDefenderControlsTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.tap();
        Permanent blocker = addReadyBlocker();
        Permanent homunculus = addAttackingHomunculus();
        beginBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(homunculus))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hazy Homunculus can be blocked when defending player controls no lands")
    void canBeBlockedWhenDefenderControlsNoLand() {
        Permanent blocker = addReadyBlocker();
        Permanent homunculus = addAttackingHomunculus();
        beginBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(homunculus))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private Permanent addAttackingHomunculus() {
        Permanent homunculus = new Permanent(new HazyHomunculus());
        homunculus.setSummoningSick(false);
        homunculus.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(homunculus);
        return homunculus;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
