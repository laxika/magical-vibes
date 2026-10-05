package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed(LionheartMaverick.class)
class LionheartMaverickTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent maverick = addCreatureReady(player1, new LionheartMaverick());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isEqualTo(1);
        assertThat(maverick.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple activations give a cumulative boost")
    void repeatedActivationsStack() {
        Permanent maverick = addCreatureReady(player1, new LionheartMaverick());
        addAbilityMana(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isEqualTo(2);
        assertThat(maverick.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent maverick = addCreatureReady(player1, new LionheartMaverick());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isZero();
        assertThat(maverick.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability requires four generic mana and one white mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new LionheartMaverick());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability cannot be paid without white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new LionheartMaverick());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Vigilance allows attacking without tapping")
    void attackingDoesNotTap() {
        Permanent maverick = addCreatureReady(player1, new LionheartMaverick());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(maverick.isAttacking()).isTrue();
        assertThat(maverick.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent maverick = harness.addToBattlefieldAndReturn(player1, new LionheartMaverick());
        maverick.setSummoningSick(true);
        maverick.setTapped(true);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isEqualTo(1);
        assertThat(maverick.getToughnessModifier()).isEqualTo(2);
        assertThat(maverick.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability boosts only its source and only after resolving")
    void boostsOnlySourceOnResolution() {
        Permanent other = addCreatureReady(player1, new LionheartMaverick());
        Permanent source = addCreatureReady(player1, new LionheartMaverick());
        Permanent opponent = addCreatureReady(player2, new LionheartMaverick());
        addAbilityMana(player1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }
}
