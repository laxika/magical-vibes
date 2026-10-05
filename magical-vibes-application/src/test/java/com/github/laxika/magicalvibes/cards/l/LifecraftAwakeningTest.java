package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.u.UniversalSolvent;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifecraftAwakening.class, UniversalSolvent.class, Ornithopter.class, ConsulateDreadnought.class, DruidOfTheCowl.class})
class LifecraftAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Puts X counters on and permanently animates a noncreature artifact")
    void putsCountersAndAnimatesNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());

        cast(2, artifact);

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
        assertThat(artifact.isPermanentlyAnimated()).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.CONSTRUCT)).isTrue();
    }

    @Test
    @DisplayName("Puts counters on an artifact creature without animating it")
    void putsCountersOnArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        cast(2, artifactCreature);

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, artifactCreature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(4);
        assertThat(artifactCreature.isPermanentlyAnimated()).isFalse();
    }

    @Test
    @DisplayName("Puts counters on a noncreature Vehicle without animating it")
    void putsCountersOnVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ConsulateDreadnought());

        cast(2, vehicle);

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(vehicle.isPermanentlyAnimated()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an artifact an opponent controls")
    void cannotTargetOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UniversalSolvent());
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("X zero animates a noncreature artifact, which dies with zero toughness")
    void zeroXAnimatesAndDies() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());

        cast(0, artifact);

        harness.assertNotOnBattlefield(player1, "Universal Solvent");
        harness.assertInGraveyard(player1, "Universal Solvent");
    }

    @Test
    @DisplayName("X zero leaves an artifact creature unchanged")
    void zeroXLeavesArtifactCreatureUnchanged() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        cast(0, artifactCreature);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A second casting adds counters to the already animated artifact")
    void repeatedCastingAddsCounters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());

        cast(2, artifact);
        cast(3, artifact);

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.CONSTRUCT)).isTrue();
    }

    @Test
    @DisplayName("Animation and counters remain through turn cleanup")
    void animationPersistsIntoNextTurn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UniversalSolvent());

        cast(2, artifact);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature you control")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int xValue, Permanent target) {
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castInstant(player1, 0, xValue, target.getId());
        harness.passBothPriorities();
    }
}
