package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodskyBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Your second spell puts two +1/+1 counters on Bloodsky Berserker and grants menace")
    void secondSpellPutsCountersAndGrantsMenace() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Menace granted by Bloodsky Berserker wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new BloodskyBerserker());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(berserker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(berserker.getGrantedKeywords()).doesNotContain(Keyword.MENACE);
    }
}
