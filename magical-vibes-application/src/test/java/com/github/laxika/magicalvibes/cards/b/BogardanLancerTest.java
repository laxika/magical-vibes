package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BogardanLancer.class, GrizzlyBears.class})
class BogardanLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1 enters with a +1/+1 counter after an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castLancer();

        assertThat(findPermanent(player1, "Bogardan Lancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 does not apply when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castLancer();

        assertThat(findPermanent(player1, "Bogardan Lancer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingDebuffsNonFlankingBlocker() {
        Permanent lancer = addCreatureReady(player1, new BogardanLancer());
        lancer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    private void castLancer() {
        harness.setHand(player1, List.of(new BogardanLancer()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
