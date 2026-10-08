package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZendikarFarguide.class, Forest.class})
class ZendikarFarguideTest extends BaseCardTest {

    @Test
    @DisplayName("Zendikar Farguide cannot be blocked when defending player controls a Forest")
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ZendikarFarguide());
        blocker.setSummoningSick(false);

        Permanent farguide = harness.addToBattlefieldAndReturn(player1, new ZendikarFarguide());
        farguide.setSummoningSick(false);
        farguide.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(farguide);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Zendikar Farguide can be blocked when defending player does not control a Forest")
    void canBeBlockedWhenDefenderDoesNotControlForest() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ZendikarFarguide());
        blocker.setSummoningSick(false);

        Permanent farguide = harness.addToBattlefieldAndReturn(player1, new ZendikarFarguide());
        farguide.setSummoningSick(false);
        farguide.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A Forest controlled only by the attacker does not prevent blocking")
    void canBeBlockedWhenOnlyAttackerControlsForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ZendikarFarguide());
        blocker.setSummoningSick(false);
        Permanent farguide = harness.addToBattlefieldAndReturn(player1, new ZendikarFarguide());
        farguide.setSummoningSick(false);
        farguide.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(farguide);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
