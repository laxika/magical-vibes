package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmorOfFaith.class, GrizzlyBears.class, FountainOfYouth.class})
class ArmorOfFaithTest extends BaseCardTest {

    // ===== Static +1/+1 boost =====

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        addArmorAttachedTo(player1, bearsPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);
    }

    // ===== Activated ability: {W} for +0/+1 until end of turn =====

    @Test
    @DisplayName("Activating ability gives enchanted creature +0/+1 until end of turn")
    void activatedAbilityBoostsToughness() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        addArmorAttachedTo(player1, bearsPerm);

        harness.addMana(player1, ManaColor.WHITE, 1);

        // Aura is at index 1 (bears at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // 2 base + 1 static + 1 from ability
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
        // Power unchanged
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, stacking the toughness boost")
    void abilityStacksMultipleActivations() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        addArmorAttachedTo(player1, bearsPerm);

        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Toughness boost wears off at end of turn but static boost remains")
    void abilityBoostWearsOffAtEndOfTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        addArmorAttachedTo(player1, bearsPerm);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Temporary boost gone, static +1/+1 still applies
        assertThat(bearsPerm.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);
    }

    // ===== Effects stop when aura removed =====

    @Test
    @DisplayName("Creature loses the static boost when Armor of Faith is removed")
    void staticBoostStopsWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = addArmorAttachedTo(player1, bearsPerm);

        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can enchant a creature")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmorOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ArmorOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Activated ability can boost a creature controlled by another player")
    void activatedAbilityBoostsCreatureControlledByAnotherPlayer() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());
        addArmorAttachedTo(player1, bearsPerm);

        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    private Permanent addArmorAttachedTo(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ArmorOfFaith());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
