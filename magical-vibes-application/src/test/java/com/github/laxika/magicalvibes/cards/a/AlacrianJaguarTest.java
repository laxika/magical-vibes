package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlacrianJaguar.class})
class AlacrianJaguarTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 1 taps another creature and saddles Alacrian Jaguar")
    void saddleTapsAnotherCreature() {
        Permanent jaguar = addCreatureReady(player1, new AlacrianJaguar());
        Permanent helper = addCreatureReady(player1, new AlacrianJaguar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jaguar.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled gives Alacrian Jaguar +2/+2 until end of turn")
    void attacksWhileSaddled() {
        Permanent jaguar = addCreatureReady(player1, new AlacrianJaguar());
        addCreatureReady(player1, new AlacrianJaguar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, jaguar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, jaguar)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(jaguar.isSaddled()).isFalse();
        assertThat(gqs.getEffectivePower(gd, jaguar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jaguar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Alacrian Jaguar does not get the attack bonus unless it was saddled when it attacked")
    void doesNotTriggerWhenNotSaddled() {
        Permanent jaguar = addCreatureReady(player1, new AlacrianJaguar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, jaguar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jaguar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Saddle cannot tap the Mount itself")
    void saddleNeedsAnotherCreature() {
        addCreatureReady(player1, new AlacrianJaguar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the saddle cost")
    void summoningSickCreatureCanSaddle() {
        Permanent jaguar = addCreatureReady(player1, new AlacrianJaguar());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new AlacrianJaguar());

        harness.activateAbility(player1, 0, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(jaguar.isSaddled()).isFalse();

        harness.passBothPriorities();

        assertThat(jaguar.isSaddled()).isTrue();
        assertThat(jaguar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay the saddle cost")
    void saddleRejectsIneligibleHelpers() {
        Permanent jaguar = addCreatureReady(player1, new AlacrianJaguar());
        Permanent tappedHelper = addCreatureReady(player1, new AlacrianJaguar());
        tappedHelper.tap();
        Permanent opposingHelper = addCreatureReady(player2, new AlacrianJaguar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(jaguar.isSaddled()).isFalse();
        assertThat(opposingHelper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Saddle cannot be activated during combat")
    void saddleRequiresSorceryTiming() {
        Permanent jaguar = addCreatureReady(player1, new AlacrianJaguar());
        Permanent helper = addCreatureReady(player1, new AlacrianJaguar());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(jaguar.isSaddled()).isFalse();
    }
}
