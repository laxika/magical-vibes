package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshenmoorCohort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrosiveMentorTest extends BaseCardTest {

    // ===== Grant: "Black creatures you control have wither" =====

    @Test
    @DisplayName("Corrosive Mentor grants itself wither (it is black)")
    void grantsSelfWither() {
        Permanent mentor = addReady(player1, new CorrosiveMentor());

        assertThat(gqs.hasKeyword(gd, mentor, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Grants wither to another black creature you control, and revokes it when it leaves")
    void grantsWitherToOtherBlackCreature() {
        Permanent mentor = addReady(player1, new CorrosiveMentor());
        Permanent blackCreature = addReady(player1, new AshenmoorCohort());

        assertThat(gqs.hasKeyword(gd, blackCreature, Keyword.WITHER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(mentor);

        assertThat(gqs.hasKeyword(gd, blackCreature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Does not grant wither to a non-black creature")
    void doesNotGrantToNonBlackCreature() {
        addReady(player1, new CorrosiveMentor());
        Permanent greenCreature = addReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, greenCreature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Does not grant wither to an opponent's black creature")
    void doesNotGrantToOpponentBlackCreature() {
        addReady(player1, new CorrosiveMentor());
        Permanent opponentBlack = addReady(player2, new AshenmoorCohort());

        assertThat(gqs.hasKeyword(gd, opponentBlack, Keyword.WITHER)).isFalse();
    }

    // ===== Behavior: wither deals combat damage as -1/-1 counters =====

    @Test
    @DisplayName("A wither creature deals combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusCountersToBlocker() {
        Permanent mentor = addReady(player1, new CorrosiveMentor()); // 1/3, black → has wither
        mentor.setAttacking(true);

        Permanent blocker = addReady(player2, new GrizzlyBears()); // 2/2
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // 1 power dealt as a -1/-1 counter rather than marked damage; blocker survives as a 1/1.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Wither does not poison players — combat damage to a player is normal life loss")
    void witherDoesNotPoisonPlayer() {
        harness.setLife(player2, 20);

        Permanent mentor = addReady(player1, new CorrosiveMentor()); // 1/3, black → has wither
        mentor.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
