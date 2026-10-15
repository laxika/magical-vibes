package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StuffedBear.class)
class StuffedBearTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Stuffed Bear makes it a 4/4 green Bear artifact creature")
    void activationAnimatesStuffedBear() {
        Permanent bear = addStuffedBear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bear)).isTrue();
        assertThat(gqs.isArtifact(bear)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.GREEN);
        assertThat(bear.getTransientSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Stuffed Bear stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent bear = addStuffedBear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bear)).isFalse();
        assertThat(gqs.isArtifact(bear)).isTrue();
        assertThat(bear.getTransientSubtypes()).doesNotContain(CardSubtype.BEAR);
        assertThat(gqs.getEffectiveColors(gd, bear)).isEmpty();
    }

    @Test
    @DisplayName("Animation waits for resolution and affects only its source")
    void animationWaitsForResolutionAndAffectsOnlySource() {
        Permanent bear = addStuffedBear();
        Permanent otherBear = addStuffedBear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.isCreature(gd, bear)).isFalse();
        assertThat(gqs.isCreature(gd, otherBear)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bear)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, otherBear)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, otherBear)).isEmpty();
    }

    @Test
    @DisplayName("A tapped newly entered Stuffed Bear can activate repeatedly without growing")
    void tappedBearCanActivateRepeatedly() {
        Permanent bear = addStuffedBear();
        bear.setSummoningSick(true);
        bear.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, bear)).isTrue();
        assertThat(gqs.isArtifact(bear)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.GREEN);
        assertThat(bear.getTransientSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    private Permanent addStuffedBear() {
        return harness.addToBattlefieldAndReturn(player1, new StuffedBear());
    }
}
