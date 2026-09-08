package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubtleStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Weakening mode gives target creature -1/-1 until end of turn")
    void weakeningMode() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castModes(new int[]{0}, List.of(target.getId()));

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Strengthening mode puts a +1/+1 counter on target creature")
    void strengtheningMode() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castModes(new int[]{1}, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Both modes can target the same creature")
    void bothModesTargetSameCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castModes(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Modes cannot target a noncreature permanent")
    void modesRejectNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SubtleStrike()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castModes(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SubtleStrike()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
