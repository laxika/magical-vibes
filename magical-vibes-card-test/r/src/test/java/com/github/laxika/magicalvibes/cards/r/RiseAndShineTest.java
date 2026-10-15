package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ClayStatue;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SilverbluffBridge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseAndShine.class, Millstone.class, ClayStatue.class, SilverbluffBridge.class,
        HardenedScales.class})
class RiseAndShineTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a target noncreature artifact and puts four +1/+1 counters on it")
    void animatesTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an artifact creature or an artifact an opponent controls")
    void targetMustBeNoncreatureArtifactYouControl() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new ClayStatue());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact you control");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact you control");
    }

    @Test
    @DisplayName("Overload animates each noncreature artifact you control and excludes existing artifact creatures")
    void overloadAnimatesEachControlledNoncreatureArtifact() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent existingArtifactCreature = harness.addToBattlefieldAndReturn(player1, new ClayStatue());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        for (Permanent artifact : java.util.List.of(first, second)) {
            assertThat(gqs.isArtifact(gd, artifact)).isTrue();
            assertThat(gqs.isCreature(gd, artifact)).isTrue();
            assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
            assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        }
        assertThat(existingArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
    }

    @Test
    @DisplayName("Animation and counters remain after cleanup")
    void animationRemainsAfterCleanup() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Overloaded animation persists and does not affect artifacts entering later")
    void overloadAnimationPersistsForOriginalArtifactsOnly() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SilverbluffBridge());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        Permanent laterArtifact = harness.addToBattlefieldAndReturn(player1, new SilverbluffBridge());
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.isLand(gd, artifact)).isTrue();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, laterArtifact)).isFalse();
        assertThat(laterArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Target artifact is a creature when Hardened Scales modifies counter placement")
    void targetIsAnimatedBeforeCountersArePlaced() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SilverbluffBridge());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
    }

    @Test
    @DisplayName("Overload animates artifacts before Hardened Scales modifies counter placement")
    void overloadAnimatesBeforeCountersArePlaced() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SilverbluffBridge());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SilverbluffBridge());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        for (Permanent artifact : java.util.List.of(first, second)) {
            assertThat(gqs.isCreature(gd, artifact)).isTrue();
            assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
            assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("Overload can resolve without any noncreature artifacts")
    void overloadResolvesWithoutEligibleArtifacts() {
        harness.setHand(player1, java.util.List.of(new RiseAndShine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rise and Shine");
        assertThat(gd.stack).isEmpty();
    }
}
