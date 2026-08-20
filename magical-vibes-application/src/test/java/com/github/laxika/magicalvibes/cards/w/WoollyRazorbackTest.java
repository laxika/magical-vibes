package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WoollyRazorbackTest extends BaseCardTest {

    @Test
    @DisplayName("Woolly Razorback enters with ice counters and has defender while it has one")
    void entersWithIceCountersAndHasDefender() {
        Permanent razorback = addRazorback();

        assertThat(razorback.getCounterCount(CounterType.ICE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, razorback, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Woolly Razorback prevents its combat damage but not damage dealt to it")
    void preventsItsCombatDamageOnly() {
        Permanent razorback = addRazorback();
        Permanent attacker = addAttacker();
        declareBlocks(razorback, List.of(attacker));

        assertThat(razorback.getCounterCount(CounterType.ICE)).isEqualTo(2);
        assertThat(razorback.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The third block removes the last ice counter before Woolly Razorback deals damage")
    void thirdBlockRemovesLastCounterBeforeCombatDamage() {
        Permanent razorback = addRazorback();
        Permanent attackerOne = addAttacker();
        declareBlocks(razorback, List.of(attackerOne));
        Permanent attackerTwo = addAttacker();
        declareBlocks(razorback, List.of(attackerTwo));
        Permanent attackerThree = addAttacker();
        declareBlocks(razorback, List.of(attackerThree));

        assertThat(razorback.getCounterCount(CounterType.ICE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(attackerThree)
                .contains(attackerOne, attackerTwo);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player2, new RagingGoblin());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addRazorback() {
        harness.setHand(player1, List.of(new WoollyRazorback()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Woolly Razorback");
    }

    private void declareBlocks(Permanent blocker, List<Permanent> attackers) {
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, attackers.stream()
                .map(attacker -> new BlockerAssignment(blockerIndex,
                        gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))
                .toList());
        harness.passBothPriorities();
        resolveAllTriggers();
        resolveCombat(player2);
        resolveAllTriggers();
    }
}
