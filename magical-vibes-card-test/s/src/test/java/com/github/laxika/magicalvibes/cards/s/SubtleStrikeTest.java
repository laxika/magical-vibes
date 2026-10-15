package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.n.NinthBridgePatrol;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SubtleStrike.class, GrizzlyBears.class, FountainOfYouth.class,
        DukharaPeafowl.class, NinthBridgePatrol.class})
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


    @Test
    @DisplayName("Both modes affect separate creatures with different controllers")
    void bothModesTargetDifferentCreatures() {
        Permanent weakened = addCreatureReady(player2, new DukharaPeafowl());
        Permanent strengthened = addCreatureReady(player1, new DukharaPeafowl());
        castModes(new int[]{0, 1}, List.of(weakened.getId(), strengthened.getId()));

        assertThat(weakened.getPowerModifier()).isEqualTo(-1);
        assertThat(weakened.getToughnessModifier()).isEqualTo(-1);
        assertThat(weakened.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(strengthened.getPowerModifier()).isZero();
        assertThat(strengthened.getToughnessModifier()).isZero();
        assertThat(strengthened.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A one-toughness creature survives when both modes target it")
    void bothModesSaveOneToughnessCreature() {
        Permanent target = addCreatureReady(player2, new NinthBridgePatrol());
        castModes(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        harness.assertOnBattlefield(player2, "Ninth Bridge Patrol");
        harness.assertNotInGraveyard(player2, "Ninth Bridge Patrol");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Weakening alone kills a creature with one toughness")
    void weakeningKillsOneToughnessCreature() {
        Permanent target = addCreatureReady(player2, new NinthBridgePatrol());
        castModes(new int[]{0}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Ninth Bridge Patrol");
        harness.assertInGraveyard(player2, "Ninth Bridge Patrol");
    }

    @Test
    @DisplayName("The weakening expires at cleanup while the counter remains")
    void weakeningExpiresButCounterRemains() {
        Permanent target = addCreatureReady(player2, new DukharaPeafowl());
        castModes(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter mode resolves when the weakening target leaves")
    void strengtheningResolvesWhenWeakeningTargetLeaves() {
        Permanent removed = addCreatureReady(player2, new NinthBridgePatrol());
        Permanent remaining = addCreatureReady(player1, new DukharaPeafowl());
        castWithResponseRemovingTarget(List.of(removed.getId(), remaining.getId()), removed.getId());

        harness.assertInGraveyard(player2, "Ninth Bridge Patrol");
        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(remaining.getPowerModifier()).isZero();
        assertThat(remaining.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The weakening mode resolves when the counter target leaves")
    void weakeningResolvesWhenStrengtheningTargetLeaves() {
        Permanent remaining = addCreatureReady(player1, new DukharaPeafowl());
        Permanent removed = addCreatureReady(player2, new NinthBridgePatrol());
        castWithResponseRemovingTarget(List.of(remaining.getId(), removed.getId()), removed.getId());

        harness.assertInGraveyard(player2, "Ninth Bridge Patrol");
        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(remaining.getPowerModifier()).isEqualTo(-1);
        assertThat(remaining.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Neither mode resolves when their shared target leaves")
    void bothModesFailWhenSharedTargetLeaves() {
        Permanent removed = addCreatureReady(player2, new NinthBridgePatrol());
        castWithResponseRemovingTarget(List.of(removed.getId(), removed.getId()), removed.getId());

        harness.assertInGraveyard(player2, "Ninth Bridge Patrol");
        assertThat(removed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(removed.getPowerModifier()).isEqualTo(-1);
        harness.assertInGraveyard(player1, "Subtle Strike");
        assertThat(gd.stack).isEmpty();
    }

    private void castWithResponseRemovingTarget(List<UUID> targetIds, UUID removedTargetId) {
        harness.setHand(player1, List.of(new SubtleStrike()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, targetIds);

        harness.setHand(player2, List.of(new SubtleStrike()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(removedTargetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castModes(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SubtleStrike()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
