package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwoByFour.class, GrizzlyBears.class})
class TwoByFourTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a base power 4 counter on a target creature")
    void putsBasePowerCounter() {
        Permanent target = addCreature();

        cast(new int[]{0}, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.BASE_POWER_FOUR)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts a base toughness 4 counter on a target creature")
    void putsBaseToughnessCounter() {
        Permanent target = addCreature();

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.BASE_TOUGHNESS_FOUR)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolves both modes on the same target")
    void resolvesBothModes() {
        Permanent target = addCreature();

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new TwoByFour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
