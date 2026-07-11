package com.github.laxika.magicalvibes.cards.z;

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

class ZhangFeiFierceWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Horsemanship: Zhang Fei can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent zhangFei = new Permanent(new ZhangFeiFierceWarrior());
        zhangFei.setSummoningSick(false);
        zhangFei.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(zhangFei);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(zhangFei);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Horsemanship: Zhang Fei can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = new Permanent(new ZhangFeiFierceWarrior());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent zhangFei = new Permanent(new ZhangFeiFierceWarrior());
        zhangFei.setSummoningSick(false);
        zhangFei.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(zhangFei);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(zhangFei);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance: Zhang Fei does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent zhangFei = new Permanent(new ZhangFeiFierceWarrior());
        zhangFei.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(zhangFei);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(zhangFei.isTapped()).isFalse();
    }
}
