package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulKiss.class, BalduvianBears.class, IcyManipulator.class})
class SoulKissTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives enchanted creature +2/+2 and costs 1 life")
    void activatedAbilityBoostsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulKiss());
        auraPerm.setAttachedTo(bearsPerm.getId());

        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Ability stacks across multiple activations")
    void abilityStacksMultipleActivations() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulKiss());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ability can be activated no more than three times each turn")
    void limitedToThreeActivationsPerTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulKiss());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 3");
    }

    @Test
    @DisplayName("Activation limit resets on the next turn")
    void activationLimitResetsOnNextTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulKiss());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 3");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new BalduvianBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulKiss());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bearsPerm.getPowerModifier()).isEqualTo(0);
        assertThat(bearsPerm.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new SoulKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new SoulKiss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
