package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivsEmbrace.class, GoblinRaider.class, WornPowerstone.class})
class ShivsEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Shiv's Embrace puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());

        harness.setHand(player1, List.of(new ShivsEmbrace()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ShivsEmbrace.class);
    }

    @Test
    @DisplayName("Resolving Shiv's Embrace attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());

        harness.setHand(player1, List.of(new ShivsEmbrace()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ShivsEmbrace
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can activate firebreathing for +1/+0")
    void grantedAbilityBoostsPower() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 2 base + 2 aura + 1 firebreathing = 5 power
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        // Toughness unaffected by firebreathing: 2 base + 2 aura = 4
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate firebreathing multiple times")
    void canActivateFirebreathingMultipleTimes() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 2 base + 2 aura + 3 firebreathing = 7 power
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Firebreathing boost resets at end of turn")
    void firebreathingResetsAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // After end of turn, only the static +2/+2 remains
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate firebreathing without enough mana")
    void cannotActivateWithoutMana() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating firebreathing does not tap the creature")
    void firebreathingDoesNotTap() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature loses boost, flying, and firebreathing when Shiv's Embrace is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        Permanent aura = addAttachedEmbrace(creature);

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        // Remove Shiv's Embrace
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        // Creature should no longer have an activated ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Can target a creature with Shiv's Embrace")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        harness.setHand(player1, List.of(new ShivsEmbrace()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Shiv's Embrace")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new ShivsEmbrace()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Shiv's Embrace does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GoblinRaider());
        Permanent otherCreature = addCreatureReady(player1, new GoblinRaider());
        addAttachedEmbrace(creature);

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Shiv's Embrace applies its static effects to an opponent's creature")
    void effectsApplyToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GoblinRaider());
        addAttachedEmbrace(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Aura controller can activate firebreathing on an opponent's creature")
    void auraControllerCanActivateFirebreathingOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GoblinRaider());
        addAttachedEmbrace(creature);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature's controller cannot activate Shiv's Embrace")
    void enchantedCreatureControllerCannotActivateFirebreathing() {
        Permanent creature = addCreatureReady(player2, new GoblinRaider());
        addAttachedEmbrace(creature);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addAttachedEmbrace(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShivsEmbrace());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
