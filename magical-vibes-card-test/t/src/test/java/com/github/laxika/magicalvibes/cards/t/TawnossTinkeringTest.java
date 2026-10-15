package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({TawnossTinkering.class, ArgothianSprite.class, EnergyRefractor.class, Forest.class})
class TawnossTinkeringTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two counters on and untaps a creature without animating it")
    void boostsAndUntapsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        creature.tap();

        cast(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(creature.isPermanentlyAnimated()).isFalse();
    }

    @Test
    @DisplayName("Animates a noncreature artifact into a permanent 0/0 creature")
    void animatesArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        artifact.tap();

        cast(artifact);

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
        assertThat(artifact.isPermanentlyAnimated()).isTrue();
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
    }

    @Test
    @DisplayName("Animates a land while keeping it a land")
    void animatesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        cast(land);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(land.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new TawnossTinkering()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Animation and counters remain after turn cleanup")
    void animationSurvivesCleanup() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(land);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting again on an animated land adds counters and untaps it")
    void repeatedCastOnAnimatedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        cast(land);
        land.tap();

        cast(land);

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TawnossTinkering()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
