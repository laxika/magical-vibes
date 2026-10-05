package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PackMastiff.class, GreenwoodSentinel.class})
class PackMastiffTest extends BaseCardTest {

    @Test
    @DisplayName("Activation boosts each Pack Mastiff you control")
    void boostsPackMastiffsYouControl() {
        Permanent mastiff = addCreatureReady(player1, new PackMastiff());
        Permanent otherMastiff = addCreatureReady(player1, new PackMastiff());
        Permanent ownSentinel = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent opponentMastiff = addCreatureReady(player2, new PackMastiff());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherMastiff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mastiff)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownSentinel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMastiff)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activation boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mastiff = addCreatureReady(player1, new PackMastiff());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boosted creatures are determined at resolution")
    void determinesBoostedCreaturesAtResolution() {
        Permanent mastiff = addCreatureReady(player1, new PackMastiff());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(2);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new PackMastiff());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new PackMastiff());

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped summoning-sick Mastiff can activate repeatedly and the boosts stack")
    void repeatedActivationsStackWithoutTappingCost() {
        Permanent mastiff = harness.addToBattlefieldAndReturn(player1, new PackMastiff());
        mastiff.setSummoningSick(true);
        mastiff.setTapped(true);
        Permanent otherMastiff = addCreatureReady(player1, new PackMastiff());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherMastiff)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mastiff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherMastiff)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherMastiff)).isEqualTo(2);
    }
}
