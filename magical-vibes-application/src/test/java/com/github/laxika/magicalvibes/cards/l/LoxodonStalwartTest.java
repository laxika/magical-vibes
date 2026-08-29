package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoxodonStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Loxodon Stalwart +0/+1")
    void resolvingAbilityBoostsToughness() {
        addLoxodonStalwartReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent stalwart = getLoxodonStalwart(player1);
        assertThat(stalwart.getEffectivePower()).isEqualTo(3);
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple activations accumulate")
    void canActivateMultipleTimes() {
        addLoxodonStalwartReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent stalwart = getLoxodonStalwart(player1);
        assertThat(stalwart.getEffectivePower()).isEqualTo(3);
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addLoxodonStalwartReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent stalwart = getLoxodonStalwart(player1);
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stalwart.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability fizzles if Loxodon Stalwart leaves before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addLoxodonStalwartReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without white mana")
    void cannotActivateWithoutEnoughMana() {
        addLoxodonStalwartReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addLoxodonStalwartReady(Player player) {
        LoxodonStalwart card = new LoxodonStalwart();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent getLoxodonStalwart(Player player) {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player.getId()).getFirst();
    }
}
