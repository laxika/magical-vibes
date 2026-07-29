package com.github.laxika.magicalvibes.cards.f;

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

class FetidHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Fetid Horror")
    void resolvingAbilityBoosts() {
        addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent horror = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(horror.getEffectivePower()).isEqualTo(2);
        assertThat(horror.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly if mana allows")
    void canActivateMultipleTimes() {
        addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent horror = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(horror.getEffectivePower()).isEqualTo(4);
        assertThat(horror.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent horror = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(horror.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(horror.getPowerModifier()).isEqualTo(0);
        assertThat(horror.getEffectivePower()).isEqualTo(1);
        assertThat(horror.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without black mana")
    void cannotActivateWithoutMana() {
        addHorrorReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addHorrorReady(Player player) {
        Permanent perm = new Permanent(new FetidHorror());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
