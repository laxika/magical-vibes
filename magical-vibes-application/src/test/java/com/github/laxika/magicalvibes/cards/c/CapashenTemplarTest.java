package com.github.laxika.magicalvibes.cards.c;

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

class CapashenTemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Capashen Templar +0/+1")
    void resolvingAbilityBoostsToughness() {
        addCapashenTemplarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent templar = getCapashenTemplar(player1);
        assertThat(templar.getEffectivePower()).isEqualTo(2);
        assertThat(templar.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple activations accumulate")
    void canActivateMultipleTimes() {
        addCapashenTemplarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent templar = getCapashenTemplar(player1);
        assertThat(templar.getEffectivePower()).isEqualTo(2);
        assertThat(templar.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCapashenTemplarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent templar = getCapashenTemplar(player1);
        assertThat(templar.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(templar.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability fizzles if Capashen Templar leaves before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCapashenTemplarReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without white mana")
    void cannotActivateWithoutEnoughMana() {
        addCapashenTemplarReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addCapashenTemplarReady(Player player) {
        CapashenTemplar card = new CapashenTemplar();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent getCapashenTemplar(Player player) {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player.getId()).getFirst();
    }
}
