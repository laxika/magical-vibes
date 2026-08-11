package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarNameAspirantTest extends BaseCardTest {

    @Test
    void entersWithoutRaidWithoutCounter() {
        castAspirant(false);

        Permanent aspirant = findAspirant();
        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithRaidWithCounter() {
        castAspirant(true);

        Permanent aspirant = findAspirant();
        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotBeBlockedByCreatureWithPowerOneOrLess() {
        Permanent blocker = new Permanent(new FugitiveWizard());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        Permanent attacker = addAttackingAspirant();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithPowerGreaterThanOne() {
        Permanent blocker = new Permanent(new HillGiant());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        Permanent attacker = addAttackingAspirant();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castAspirant(boolean raid) {
        if (raid) {
            gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        }
        harness.setHand(player1, List.of(new WarNameAspirant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findAspirant() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("War-Name Aspirant"))
                .findFirst()
                .orElseThrow();
    }

    private Permanent addAttackingAspirant() {
        Permanent attacker = new Permanent(new WarNameAspirant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
