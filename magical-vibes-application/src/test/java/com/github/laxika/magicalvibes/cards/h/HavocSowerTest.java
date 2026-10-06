package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HavocSower.class)
class HavocSowerTest extends BaseCardTest {

    @Test
    @DisplayName("Havoc Sower gets +2/+1 after paying its colorless activation cost")
    void boostsItself() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sower.getEffectivePower()).isEqualTo(5);
        assertThat(sower.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Havoc Sower's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(sower.getEffectivePower()).isEqualTo(3);
        assertThat(sower.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Havoc Sower requires colorless mana for its activation")
    void requiresColorlessMana() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(sower.getEffectivePower()).isEqualTo(3);
        assertThat(sower.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Colored mana can pay the generic part of the activation cost")
    void acceptsMixedMana() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sower.getEffectivePower()).isEqualTo(5);
        assertThat(sower.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack and boost only their source")
    void repeatedActivationsBoostOnlySource() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        Permanent other = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sower.getEffectivePower()).isEqualTo(7);
        assertThat(sower.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(3);
        assertThat(other.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability works while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent sower = harness.addToBattlefieldAndReturn(player1, new HavocSower());
        sower.setSummoningSick(true);
        sower.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sower.getEffectivePower()).isEqualTo(5);
        assertThat(sower.getEffectiveToughness()).isEqualTo(4);
        assertThat(sower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One colorless mana cannot pay the entire activation cost")
    void requiresGenericManaAsWell() {
        Permanent sower = addCreatureReady(player1, new HavocSower());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(sower.getEffectivePower()).isEqualTo(3);
        assertThat(sower.getEffectiveToughness()).isEqualTo(3);
    }
}
