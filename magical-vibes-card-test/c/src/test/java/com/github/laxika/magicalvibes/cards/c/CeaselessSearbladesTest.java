package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.Rootwalla;
import com.github.laxika.magicalvibes.cards.f.FireBellyChangeling;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.cards.w.WaterServant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CeaselessSearblades.class, WaterServant.class, Rootwalla.class,
        FireBellyChangeling.class, Smokebraider.class})
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

    @Test
    @DisplayName("The boost trigger resolves before the activated ability")
    void triggerResolvesBeforeActivatedAbility() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        Permanent servant = addCreatureReady(player1, new WaterServant());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(searblades.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(searblades.getPowerModifier()).isEqualTo(1);
        assertThat(servant.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        resolveAllTriggers();
        assertThat(servant.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent activating an Elemental ability does not trigger the boost")
    void opponentActivationDoesNotTrigger() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player2, new FireBellyChangeling());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        resolveAllTriggers();
        assertThat(searblades.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Changeling makes an activated ability source an Elemental")
    void changelingActivationTriggersBoost() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(searblades.getPowerModifier()).isEqualTo(1);
        assertThat(searblades.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each Ceaseless Searblades triggers independently")
    void eachSearbladesTriggers() {
        Permanent first = addCreatureReady(player1, new CeaselessSearblades());
        Permanent second = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating an Elemental mana ability also triggers the boost")
    void manaAbilityTriggersBoost() {
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new Smokebraider());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(searblades.getPowerModifier()).isEqualTo(1);
        assertThat(searblades.getToughnessModifier()).isZero();
    }
}
