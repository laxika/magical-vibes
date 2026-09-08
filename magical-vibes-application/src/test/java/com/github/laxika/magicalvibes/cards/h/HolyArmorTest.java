package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JandorsRing;
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

@CardUsed({HolyArmor.class, GrizzlyBears.class, JandorsRing.class})
class HolyArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting Holy Armor attaches it to the target creature")
    void castingAttachesToTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Holy Armor");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activating ability gives enchanted creature +0/+1 until end of turn")
    void activatedAbilityBoostsToughness() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);

        // Aura is at index 1 (bears at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // 2 base + 2 static + 1 from ability
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
        // Power unchanged
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, stacking the toughness boost")
    void abilityStacksMultipleActivations() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // 2 base + 2 static + 2 from abilities
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Toughness boost wears off at end of turn but static boost remains")
    void abilityBoostWearsOffAtEndOfTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Temporary boost gone, static +0/+2 still applies
        assertThat(bearsPerm.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature loses the static boost when Holy Armor is removed")
    void staticBoostStopsWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability boosts a creature controlled by an opponent")
    void activatedAbilityBoostsOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyArmor());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new JandorsRing());
        harness.setHand(player1, List.of(new HolyArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Jandor's Ring");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
