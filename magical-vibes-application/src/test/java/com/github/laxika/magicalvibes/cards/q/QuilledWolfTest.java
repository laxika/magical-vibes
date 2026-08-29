package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuilledWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +4/+4 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent wolf = addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(4);
        assertThat(wolf.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack")
    void repeatedActivationsStack() {
        Permanent wolf = addReadyWolf(player1);
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
        Permanent wolf = addReadyWolf(player1);
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
        addReadyWolf(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyWolf(Player player) {
        Permanent perm = new Permanent(new QuilledWolf());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
