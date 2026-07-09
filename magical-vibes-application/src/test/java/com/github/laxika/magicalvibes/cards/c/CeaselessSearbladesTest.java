package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.Rootwalla;
import com.github.laxika.magicalvibes.cards.w.WaterServant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CeaselessSearbladesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating an Elemental's ability triggers a +1/+0 trigger on the stack")
    void activatingElementalAbilityPutsTriggerOnStack() {
        addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new WaterServant()); // Elemental
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null); // Water Servant's {U}: +1/-1

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Ceaseless Searblades"));
    }

    @Test
    @DisplayName("Gets +1/+0 when you activate an ability of an Elemental")
    void boostsWhenElementalAbilityActivated() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new WaterServant()); // Elemental
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(searblades.getPowerModifier()).isEqualTo(1);
        assertThat(searblades.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Stacks for each Elemental ability activation")
    void boostStacksAcrossActivations() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new WaterServant()); // Elemental
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(searblades.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a non-Elemental's ability is activated")
    void noBoostForNonElementalAbility() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new Rootwalla()); // Lizard, not Elemental
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null); // {1}{G}: +2/+2
        resolveAllTriggers();

        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Ceaseless Searblades"));
        assertThat(searblades.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new WaterServant()); // Elemental
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(searblades.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(searblades.getPowerModifier()).isEqualTo(0);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
