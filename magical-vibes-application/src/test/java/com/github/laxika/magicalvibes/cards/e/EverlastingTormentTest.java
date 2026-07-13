package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EverlastingTormentTest extends BaseCardTest {

    @Test
    @DisplayName("All damage is dealt with wither: a non-wither creature's combat damage becomes -1/-1 counters")
    void allDamageDealtWithWither() {
        // Attacker at battlefield index 0 (blocking targets reference the attacker's battlefield index).
        Permanent attacker = new Permanent(new GrizzlyBears()); // 2/2, no native wither
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EverlastingTorment()));

        Permanent blocker = new Permanent(new HillGiant()); // 3/3 → survives, so counters persist
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2 power dealt as -1/-1 counters rather than marked damage; blocker survives as a 1/1.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Players can't gain life")
    void playersCantGainLife() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EverlastingTorment()));

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Damage can't be prevented")
    void damageCantBePrevented() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EverlastingTorment()));

        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }
}
