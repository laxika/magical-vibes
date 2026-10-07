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

@CardUsed({TwoByFour.class, GrizzlyBears.class, TurnToFrog.class})
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

    @Test
    @DisplayName("Both modes put their counters on separate creatures")
    void resolvesBothModesOnDifferentTargets() {
        Permanent powerTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent toughnessTarget = addCreature();

        cast(new int[]{0, 1}, List.of(powerTarget.getId(), toughnessTarget.getId()));

        assertThat(powerTarget.getCounterCount(CounterType.BASE_POWER_FOUR)).isEqualTo(1);
        assertThat(powerTarget.getCounterCount(CounterType.BASE_TOUGHNESS_FOUR)).isZero();
        assertThat(gqs.getEffectivePower(gd, powerTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, powerTarget)).isEqualTo(2);
        assertThat(toughnessTarget.getCounterCount(CounterType.BASE_POWER_FOUR)).isZero();
        assertThat(toughnessTarget.getCounterCount(CounterType.BASE_TOUGHNESS_FOUR)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, toughnessTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toughnessTarget)).isEqualTo(4);
    }

    @Test
    @DisplayName("A remaining legal target receives only its selected mode's counter")
    void resolvesOnlyModeWithRemainingLegalTarget() {
        Permanent powerTarget = addCreature();
        Permanent toughnessTarget = addCreature();
        harness.setHand(player1, List.of(new TwoByFour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(powerTarget.getId(), toughnessTarget.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(powerTarget);
        gd.playerGraveyards.get(player2.getId()).add(powerTarget.getCard());
        harness.passBothPriorities();

        assertThat(toughnessTarget.getCounterCount(CounterType.BASE_POWER_FOUR)).isZero();
        assertThat(toughnessTarget.getCounterCount(CounterType.BASE_TOUGHNESS_FOUR)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, toughnessTarget)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toughnessTarget)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base counters do not replace existing power and toughness bonuses")
    void preservesPlusOnePlusOneCounters() {
        Permanent target = addCreature();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Multiple base counters set the same value rather than adding together")
    void repeatedCountersDoNotStackPowerOrToughness() {
        Permanent target = addCreature();

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));
        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        assertThat(target.getCounterCount(CounterType.BASE_POWER_FOUR)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.BASE_TOUGHNESS_FOUR)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("A new base power counter overrides an older base-setting effect only for power")
    void counterOverridesOlderBaseSettingEffect() {
        Permanent target = addCreature();
        turnToFrog(target);

        cast(new int[]{0}, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newer base-setting effect overrides both existing base counters")
    void newerBaseSettingEffectOverridesCounters() {
        Permanent target = addCreature();
        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        turnToFrog(target);

        assertThat(target.getCounterCount(CounterType.BASE_POWER_FOUR)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.BASE_TOUGHNESS_FOUR)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private void turnToFrog(Permanent target) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
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
