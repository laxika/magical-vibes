package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarklitGargoyle.class})
class DarklitGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        addGargoyle(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(gd.playerBattlefields.get(player1.getId()).getFirst().getCard());
    }

    @Test
    @DisplayName("Resolving the ability gives +2/-1 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent gargoyle = addGargoyle(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(2);
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("A second activation puts the Gargoyle into the graveyard for zero toughness")
    void canActivateMultipleTimesForCumulativeBoost() {
        Permanent gargoyle = addGargoyle(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gargoyle);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gargoyle.getCard());
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent gargoyle = addGargoyle(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(2);
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(0);
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addGargoyle(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addGargoyle(Player player) {
        return addCreatureReady(player, new DarklitGargoyle());
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new DarklitGargoyle());
        gargoyle.setSummoningSick(true);
        gargoyle.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.getPowerModifier()).isEqualTo(2);
        assertThat(gargoyle.getToughnessModifier()).isEqualTo(-1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gargoyle);
    }

    @Test
    @DisplayName("White mana cannot pay the black activation cost")
    void cannotActivateWithWhiteMana() {
        addGargoyle(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the source Gargoyle gets the boost")
    void doesNotBoostAnotherGargoyle() {
        Permanent source = addGargoyle(player1);
        Permanent other = addGargoyle(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isEqualTo(-1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
