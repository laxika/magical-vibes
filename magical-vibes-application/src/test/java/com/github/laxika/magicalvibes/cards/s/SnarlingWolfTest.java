package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SnarlingWolf.class)
class SnarlingWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives Snarling Wolf +2/+2 until end of turn")
    void abilityBoostsSelf() {
        Permanent wolf = addReadyWolf(player1);
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wolf = addReadyWolf(player1);
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(0);
        assertThat(wolf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addReadyWolf(player1);
        addMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent wolf = addReadyWolf(player1);
        addMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(wolf.getPowerModifier()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Snarling Wolf has its own activation limit and boosts only itself")
    void separateWolvesCanEachActivate() {
        Permanent first = addReadyWolf(player1);
        Permanent second = addReadyWolf(player1);
        addMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wolf can activate on an opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        wolf.setSummoningSick(true);
        wolf.tap();
        harness.forceActivePlayer(player2);
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activation limit resets on the next player's turn")
    void canActivateAgainNextTurn() {
        Permanent wolf = addReadyWolf(player1);
        addMana(player1, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
        addMana(player1, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(2);
        assertThat(wolf.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addReadyWolf(Player player) {
        return addCreatureReady(player, new SnarlingWolf());
    }

    private void addMana(Player player, int amount) {
        harness.addMana(player, ManaColor.COLORLESS, amount);
        harness.addMana(player, ManaColor.GREEN, amount);
    }
}
