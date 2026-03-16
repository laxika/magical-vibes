package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArrestTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Arrest has correct card properties")
    void hasCorrectProperties() {
        Arrest card = new Arrest();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(EnchantedCreatureCantAttackOrBlockEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(EnchantedCreatureCantActivateAbilitiesEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Arrest puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Arrest");
    }

    @Test
    @DisplayName("Resolving Arrest attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Arrest")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Prevents attacking =====

    @Test
    @DisplayName("Arrested creature cannot attack")
    void arrestedCreatureCannotAttack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

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
    @DisplayName("Arrested creature cannot block")
    void arrestedCreatureCannotBlock() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(blockerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(arrestPerm);

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
    @DisplayName("Arrested creature cannot activate abilities")
    void arrestedCreatureCannotActivateAbilities() {
        // Bottle Gnomes has a sacrifice ability (activated ability)
        Permanent gnomesPerm = new Permanent(new BottleGnomes());
        gnomesPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomesPerm);

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(gnomesPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        // Try to activate Bottle Gnomes' sacrifice ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Arrested creature with tap ability cannot activate it")
    void arrestedCreatureCannotActivateTapAbility() {
        // Abuna Acolyte has tap abilities
        Permanent acolytePerm = new Permanent(new AbunaAcolyte());
        acolytePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(acolytePerm);

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(acolytePerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        Permanent targetPerm = new Permanent(new GrizzlyBears());
        targetPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(targetPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Removal restores abilities =====

    @Test
    @DisplayName("Creature can activate abilities again after Arrest is removed")
    void creatureCanActivateAfterArrestRemoved() {
        Permanent gnomesPerm = new Permanent(new BottleGnomes());
        gnomesPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomesPerm);

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(gnomesPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        // Cannot activate while arrested
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        // Remove Arrest
        gd.playerBattlefields.get(player2.getId()).remove(arrestPerm);

        // Now sacrifice ability should work — Bottle Gnomes gains 3 life
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Creature can attack again after Arrest is removed")
    void creatureCanAttackAfterArrestRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Cannot attack while arrested
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        // Remove Arrest
        gd.playerBattlefields.get(player2.getId()).remove(arrestPerm);

        // Now creature can attack
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Arrest")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Arrest")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Arrest fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Arrest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Arrest"));
    }
}
