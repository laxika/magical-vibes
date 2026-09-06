package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StormShaman.class)
class StormShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/+0")
    void activatingAbilityBoostsPower() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaman.getPowerModifier()).isEqualTo(1);
        assertThat(shaman.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void requiresRedMana() {
        addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.BLUE, 1);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate multiple times — each gives +1/+0")
    void canActivateMultipleTimes() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(shaman.getPowerModifier()).isEqualTo(3);
        assertThat(shaman.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void canBeActivatedWhileSummoningSick() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaman.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaman.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shaman.getPowerModifier()).isEqualTo(0);
        assertThat(shaman.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can be activated during an opponent's turn")
    void canBeActivatedDuringOpponentsTurn() {
        Permanent shaman = addCreatureReady(player1, new StormShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaman.getPowerModifier()).isEqualTo(1);
    }
}
