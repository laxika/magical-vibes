package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TeeterpeakAmbusher.class)
class TeeterpeakAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +2/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent ambusher = addReadyAmbusher();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ambusher.getPowerModifier()).isEqualTo(2);
        assertThat(ambusher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent ambusher = addReadyAmbusher();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ambusher.getPowerModifier()).isEqualTo(0);
        assertThat(ambusher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Repeated activations stack and affect only their source")
    void repeatedActivationsBoostOnlySource() {
        Permanent ambusher = addReadyAmbusher();
        Permanent other = addReadyAmbusher();
        Permanent opponent = addCreatureReady(player2, new TeeterpeakAmbusher());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(ambusher.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(ambusher.getPowerModifier()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(ambusher.getPowerModifier()).isEqualTo(4);
        assertThat(ambusher.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ambusher can activate the ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new TeeterpeakAmbusher());
        ambusher.setSummoningSick(true);
        ambusher.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ambusher.getPowerModifier()).isEqualTo(2);
        assertThat(ambusher.getToughnessModifier()).isZero();
        assertThat(ambusher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires red mana")
    void cannotActivateWithoutRedMana() {
        Permanent ambusher = addReadyAmbusher();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(ambusher.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The ability requires three mana in total")
    void cannotActivateWithOnlyTwoMana() {
        Permanent ambusher = addReadyAmbusher();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(ambusher.getPowerModifier()).isZero();
    }

    private Permanent addReadyAmbusher() {
        return addCreatureReady(player1, new TeeterpeakAmbusher());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
