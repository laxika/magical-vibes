package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfLava.class})
class WallOfLavaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} gives +1/+1 until end of turn")
    void payManaBoostsSelf() {
        Permanent wall = addCreatureReady(player1, new WallOfLava());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ability can be activated multiple times to stack the boost")
    void stacksBoost() {
        Permanent wall = addCreatureReady(player1, new WallOfLava());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent wall = addCreatureReady(player1, new WallOfLava());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating the ability does not tap Wall of Lava")
    void activationDoesNotTapSource() {
        Permanent wall = addCreatureReady(player1, new WallOfLava());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new WallOfLava());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wall can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfLava());
        wall.setSummoningSick(true);
        wall.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost applies on resolution and only to the source Wall")
    void boostsOnlySourceOnResolution() {
        Permanent source = addCreatureReady(player1, new WallOfLava());
        Permanent other = addCreatureReady(player1, new WallOfLava());
        Permanent opponent = addCreatureReady(player2, new WallOfLava());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(3);
    }
}
