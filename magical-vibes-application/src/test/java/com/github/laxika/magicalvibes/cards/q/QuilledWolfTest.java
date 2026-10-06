package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuilledWolf.class})
class QuilledWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +4/+4 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(4);
        assertThat(wolf.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack")
    void repeatedActivationsStack() {
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.GREEN, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(8);
        assertThat(wolf.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability requires green mana even with six mana available")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new QuilledWolf());
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick wolf can activate and pay generic mana with another color")
    void tappedSummoningSickWolfCanActivate() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        wolf.setSummoningSick(true);
        wolf.tap();
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(4);
        assertThat(wolf.getToughnessModifier()).isEqualTo(4);
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability boosts only the wolf that activated it")
    void boostsOnlySourceWolf() {
        Permanent otherWolf = addCreatureReady(player1, new QuilledWolf());
        Permanent sourceWolf = addCreatureReady(player1, new QuilledWolf());
        Permanent opposingWolf = addCreatureReady(player2, new QuilledWolf());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(sourceWolf.getPowerModifier()).isEqualTo(4);
        assertThat(sourceWolf.getToughnessModifier()).isEqualTo(4);
        assertThat(otherWolf.getPowerModifier()).isZero();
        assertThat(otherWolf.getToughnessModifier()).isZero();
        assertThat(opposingWolf.getPowerModifier()).isZero();
        assertThat(opposingWolf.getToughnessModifier()).isZero();
    }
}
