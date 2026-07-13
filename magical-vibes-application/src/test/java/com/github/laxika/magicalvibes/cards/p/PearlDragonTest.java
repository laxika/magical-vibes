package com.github.laxika.magicalvibes.cards.p;

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

class PearlDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Pearl Dragon")
    void resolvingAbilityBoostsToughness() {
        addPearlDragonReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent dragon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dragon.getEffectivePower()).isEqualTo(4);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate the ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addPearlDragonReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent dragon = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dragon.getEffectiveToughness()).isEqualTo(7);
        assertThat(dragon.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addPearlDragonReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent dragon = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addPearlDragonReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addPearlDragonReady(Player player) {
        PearlDragon card = new PearlDragon();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
