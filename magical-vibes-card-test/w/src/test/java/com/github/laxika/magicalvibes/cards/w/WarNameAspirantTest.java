package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SageEyeHarrier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarNameAspirant.class, SageEyeHarrier.class, WetlandSambar.class})
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
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SageEyeHarrier());
        Permanent attacker = addAttackingAspirant();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithPowerGreaterThanOne() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent attacker = addAttackingAspirant();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        castAspirant(false);

        assertThat(findAspirant().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void raidAppliesWhenEnteringWithoutBeingCast() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        Permanent aspirant = harness.enterBattlefieldAndReturn(player1, new WarNameAspirant());

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeBlockedByPrintedPowerOneCreatureWithCounter() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SageEyeHarrier());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addAttackingAspirant();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void cannotBeBlockedByCreatureWithZeroPower() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SageEyeHarrier());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent attacker = addAttackingAspirant();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
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
        return findPermanent(player1, "War-Name Aspirant");
    }

    private Permanent addAttackingAspirant() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WarNameAspirant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
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
