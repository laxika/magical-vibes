package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({KnightOfValor.class, LongbowArcher.class})
class KnightOfValorTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking automatically weakens a blocker without flanking")
    void flankingTriggersWhenBlocked() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent blocker = addCreatureReady(player2, new LongbowArcher());
        knight.setAttacking(true);

        declareBlockers(knight, blocker);
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blockers without flanking get -1/-1")
    void blockersWithoutFlankingGetMinusOneMinusOne() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent blocker = addCreatureReady(player2, new LongbowArcher());
        setupBlocked(knight, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, battlefieldIndex(knight), null, null);
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A flanking blocker is unaffected by the ability")
    void flankingBlockerUnaffected() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent blocker = addCreatureReady(player2, new KnightOfValor());
        setupBlocked(knight, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, battlefieldIndex(knight), null, null);
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each non-flanking blocker is hit")
    void hitsEachNonFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent blocker1 = addCreatureReady(player2, new LongbowArcher());
        Permanent blocker2 = addCreatureReady(player2, new LongbowArcher());
        setupBlocked(knight, blocker1, blocker2);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, battlefieldIndex(knight), null, null);
        harness.passBothPriorities();

        assertThat(blocker1.getEffectivePower()).isEqualTo(1);
        assertThat(blocker1.getEffectiveToughness()).isEqualTo(1);
        assertThat(blocker2.getEffectivePower()).isEqualTo(1);
        assertThat(blocker2.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only creatures blocking this Knight are weakened")
    void onlyBlockersOfSourceAreAffected() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent otherKnight = addCreatureReady(player1, new KnightOfValor());
        Permanent blockerOfKnight = addCreatureReady(player2, new LongbowArcher());
        Permanent blockerOfOtherKnight = addCreatureReady(player2, new LongbowArcher());
        setupBlocked(knight, blockerOfKnight);

        otherKnight.setAttacking(true);
        blockerOfOtherKnight.setBlocking(true);
        blockerOfOtherKnight.addBlockingTarget(battlefieldIndex(otherKnight));
        blockerOfOtherKnight.addBlockingTargetId(otherKnight.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, battlefieldIndex(knight), null, null);
        harness.passBothPriorities();

        assertThat(blockerOfKnight.getEffectivePower()).isEqualTo(1);
        assertThat(blockerOfKnight.getEffectiveToughness()).isEqualTo(1);
        assertThat(blockerOfOtherKnight.getEffectivePower()).isEqualTo(2);
        assertThat(blockerOfOtherKnight.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void boostWearsOff() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent blocker = addCreatureReady(player2, new LongbowArcher());
        setupBlocked(knight, blocker);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, battlefieldIndex(knight), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfValor());
        Permanent blocker = addCreatureReady(player2, new LongbowArcher());
        setupBlocked(knight, blocker);

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, battlefieldIndex(knight), null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(knight), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getEffectivePower()).isEqualTo(1);
    }

    private void declareBlockers(Permanent attacker, Permanent... blockers) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(blockers).stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        battlefieldIndex(attacker)))
                .toList());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void setupBlocked(Permanent knight, Permanent... blockers) {
        knight.setAttacking(true);
        for (Permanent blocker : blockers) {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(battlefieldIndex(knight));
            blocker.addBlockingTargetId(knight.getId());
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
