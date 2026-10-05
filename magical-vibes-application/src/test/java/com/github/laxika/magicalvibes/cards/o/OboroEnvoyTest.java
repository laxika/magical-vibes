package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FiddleheadKami;
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

@CardUsed({OboroEnvoy.class, OboroPalaceInTheClouds.class, FiddleheadKami.class})
class OboroEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land and gives target creature -X/-0 based on hand size")
    void returnsLandAndReducesTargetPowerByHandSize() {
        Permanent envoy = addCreatureReady(player1, new OboroEnvoy());
        Permanent target = addCreatureReady(player2, new FiddleheadKami());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of(new FiddleheadKami(), new OboroEnvoy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(envoy);
        assertThat(gd.playerHands.get(player1.getId())).contains(land.getCard());
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power reduction wears off at end of turn")
    void powerReductionWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new FiddleheadKami());
        addCreatureReady(player1, new OboroEnvoy());
        harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of(new FiddleheadKami()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new OboroEnvoy());
        harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a land to pay the activation cost")
    void requiresLandToReturn() {
        addCreatureReady(player1, new OboroEnvoy());
        Permanent target = addCreatureReady(player2, new FiddleheadKami());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's land to pay the activation cost")
    void cannotReturnOpponentsLand() {
        addCreatureReady(player1, new OboroEnvoy());
        Permanent target = addCreatureReady(player2, new FiddleheadKami());
        harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses hand size at resolution and keeps that reduction after hand size changes")
    void handSizeIsDeterminedAtResolution() {
        addCreatureReady(player1, new OboroEnvoy());
        Permanent target = addCreatureReady(player2, new FiddleheadKami());
        harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FiddleheadKami(), new FiddleheadKami()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player1, List.of(new OboroEnvoy(), new FiddleheadKami(),
                new FiddleheadKami(), new OboroPalaceInTheClouds()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
    }

    @Test
    @DisplayName("A tapped Envoy can target itself and the returned land counts in hand")
    void tappedEnvoyCanTargetItself() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new OboroEnvoy());
        envoy.setTapped(true);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, envoy.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, envoy)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, envoy)).isEqualTo(3);
    }
}
