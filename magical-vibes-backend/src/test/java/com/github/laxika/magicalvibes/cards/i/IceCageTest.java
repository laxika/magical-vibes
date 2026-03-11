package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroySourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IceCageTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ice Cage has correct effects")
    void hasCorrectEffects() {
        IceCage card = new IceCage();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(EnchantedCreatureCantAttackOrBlockEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(EnchantedCreatureCantActivateAbilitiesEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY).get(0)).isInstanceOf(DestroySourcePermanentEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Ice Cage attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new IceCage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ice Cage")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Prevents attacking =====

    @Test
    @DisplayName("Ice Caged creature cannot attack")
    void iceCagedCreatureCannotAttack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(iceCagePerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Prevents blocking =====

    @Test
    @DisplayName("Ice Caged creature cannot block")
    void iceCagedCreatureCannotBlock() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(blockerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(iceCagePerm);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    // ===== Prevents activated abilities =====

    @Test
    @DisplayName("Ice Caged creature cannot activate abilities")
    void iceCagedCreatureCannotActivateAbilities() {
        Permanent gnomesPerm = new Permanent(new BottleGnomes());
        gnomesPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomesPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(gnomesPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(iceCagePerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Destroyed when enchanted creature becomes target of a spell =====

    @Test
    @DisplayName("Ice Cage is destroyed when enchanted creature becomes target of a spell")
    void destroyedWhenEnchantedCreatureTargetedBySpell() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(iceCagePerm);

        // Cast Shock targeting the Ice Caged creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsPerm.getId());

        // Stack should have Shock + Ice Cage's triggered ability on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Ice Cage's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Ice Cage should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ice Cage"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ice Cage"));
    }

    // ===== Destroyed when enchanted creature becomes target of an ability =====

    @Test
    @DisplayName("Ice Cage is destroyed when enchanted creature becomes target of an ability")
    void destroyedWhenEnchantedCreatureTargetedByAbility() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(iceCagePerm);

        // Use Icy Manipulator to target the Ice Caged creature with an activated ability
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent icyPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();
        icyPerm.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icyPerm), null, bearsPerm.getId());

        // Stack should have Icy Manipulator's ability + Ice Cage's triggered ability on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Ice Cage's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Ice Cage should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ice Cage"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ice Cage"));
    }

    // ===== Creature can attack/block after Ice Cage is destroyed =====

    @Test
    @DisplayName("Creature can attack after Ice Cage is destroyed by targeting")
    void creatureCanAttackAfterIceCageDestroyed() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(iceCagePerm);

        // Cannot attack while caged
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        // Remove Ice Cage (simulating destruction)
        gd.playerBattlefields.get(player2.getId()).remove(iceCagePerm);

        // Now creature can attack
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    // ===== Does NOT trigger when Ice Cage itself is targeted =====

    @Test
    @DisplayName("Targeting Ice Cage itself does not trigger its destruction")
    void doesNotTriggerWhenIceCageItselfIsTargeted() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent iceCagePerm = new Permanent(new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(iceCagePerm);

        // Cast Naturalize targeting Ice Cage itself (not the enchanted creature)
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, iceCagePerm.getId());

        // Stack should only have Naturalize — no Ice Cage trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Naturalize");
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Ice Cage")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new IceCage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
