package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredWhirlTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreechingPhoenix.class, ArmoredWhirlTurtle.class})
class ScreechingPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives creatures you control +1/+0")
    void boostsOwnCreatures() {
        Permanent phoenix = addPhoenix(player1);
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new ArmoredWhirlTurtle());
        Permanent opponentTurtle = harness.addToBattlefieldAndReturn(player2, new ArmoredWhirlTurtle());

        activateAbility(player1);

        assertThat(phoenix.getPowerModifier()).isEqualTo(1);
        assertThat(phoenix.getToughnessModifier()).isZero();
        assertThat(turtle.getPowerModifier()).isEqualTo(1);
        assertThat(turtle.getToughnessModifier()).isZero();
        assertThat(opponentTurtle.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Activated ability bonus wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addPhoenix(player1);
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new ArmoredWhirlTurtle());

        activateAbility(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(turtle.getPowerModifier()).isZero();
        assertThat(turtle.getToughnessModifier()).isZero();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new ScreechingPhoenix());
        phoenix.setSummoningSick(true);
        phoenix.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        activateAbility(player1);

        assertThat(phoenix.getPowerModifier()).isEqualTo(1);
        assertThat(phoenix.getToughnessModifier()).isZero();
        assertThat(phoenix.isTapped()).isTrue();
    }

    @Test
    void repeatedActivationsStack() {
        Permanent phoenix = addPhoenix(player1);

        activateAbility(player1);
        activateAbility(player1);

        assertThat(phoenix.getPowerModifier()).isEqualTo(2);
        assertThat(phoenix.getToughnessModifier()).isZero();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotThoseEnteringLater() {
        Permanent source = addPhoenix(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(source.getPowerModifier()).isZero();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new ScreechingPhoenix());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new ScreechingPhoenix());

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addPhoenix(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ScreechingPhoenix());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    void cannotPayRedRequirementWithOnlyColorlessMana() {
        Permanent phoenix = addPhoenix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(phoenix.getPowerModifier()).isZero();
    }

    private Permanent addPhoenix(Player player) {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player, new ScreechingPhoenix());
        phoenix.setSummoningSick(false);
        return phoenix;
    }

    private void activateAbility(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }
}
