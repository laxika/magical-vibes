package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BreathOfFire;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ConfidenceFromStrength.class, FountainOfYouth.class, GrizzlyBears.class})
class ConfidenceFromStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +4/+4 and trample")
    void boostsAndGrantsTrample() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConfidenceFromStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, bear.getId());

        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConfidenceFromStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, bear.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ConfidenceFromStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature without affecting another creature")
    void canTargetOpponentsCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConfidenceFromStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, opposingBear.getId());

        assertThat(opposingBear.getEffectivePower()).isEqualTo(6);
        assertThat(opposingBear.getEffectiveToughness()).isEqualTo(6);
        assertThat(opposingBear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownBear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed(BreathOfFire.class)
    @DisplayName("Does not apply either effect when its target dies before resolution")
    void targetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConfidenceFromStrength()));
        harness.setHand(player2, List.of(new BreathOfFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Confidence from Strength");
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
