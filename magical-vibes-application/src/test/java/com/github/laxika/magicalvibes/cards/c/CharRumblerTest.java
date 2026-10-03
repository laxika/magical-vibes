package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CharRumbler.class)
class CharRumblerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent charRumbler = addReadyCharRumbler(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charRumbler.getPowerModifier()).isEqualTo(1);
        assertThat(charRumbler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Repeated activations stack")
    void repeatedActivationsStack() {
        Permanent charRumbler = addReadyCharRumbler(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charRumbler.getPowerModifier()).isEqualTo(2);
        assertThat(charRumbler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent charRumbler = addReadyCharRumbler(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(charRumbler.getPowerModifier()).isZero();
        assertThat(charRumbler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyCharRumbler(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsCombatDamageTwice() {
        addReadyCharRumbler(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCharRumbler(Player player) {
        return addCreatureReady(player, new CharRumbler());
    }

    @Test
    @DisplayName("Negative power deals no combat damage in either damage step")
    void negativePowerDealsNoCombatDamage() {
        addReadyCharRumbler(player1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("One activation raises negative power to zero and still deals no damage")
    void singleBoostDealsNoCombatDamage() {
        addReadyCharRumbler(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures can activate the ability, boosting only its source")
    void abilityDoesNotRequireTappingOrHaste() {
        Permanent source = addReadyCharRumbler(player1);
        Permanent other = addReadyCharRumbler(player1);
        source.setTapped(true);
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(source.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
