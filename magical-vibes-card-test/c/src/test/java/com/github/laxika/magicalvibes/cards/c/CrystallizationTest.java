package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crystallization.class, GrizzlyBears.class, IcyManipulator.class, Naturalize.class, Shock.class})
class CrystallizationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crystallization attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new Crystallization()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Crystallization")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new Crystallization());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Crystallization());
        auraPerm.setAttachedTo(blockerPerm.getId());

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature is exiled when it becomes the target of a spell")
    void exilesEnchantedCreatureWhenTargetedBySpell() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Crystallization());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Cast Shock targeting the enchanted creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsPerm.getId());

        // Stack should have Shock + Crystallization's triggered ability on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve the triggered ability first (it's on top)
        harness.passBothPriorities();

        // The enchanted creature is exiled to its owner's exile zone
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // The now-orphaned Aura goes to its controller's graveyard
        harness.assertNotOnBattlefield(player1, "Crystallization");
        harness.assertInGraveyard(player1, "Crystallization");
    }

    @Test
    @DisplayName("Enchanted creature is exiled when it becomes the target of an ability")
    void exilesEnchantedCreatureWhenTargetedByAbility() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Crystallization());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Use Icy Manipulator to target the enchanted creature with an activated ability
        Permanent icyPerm = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icyPerm.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icyPerm), null, bearsPerm.getId());

        // Stack should have Icy Manipulator's ability + Crystallization's trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve the triggered ability first (it's on top)
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Targeting Crystallization itself does not exile the creature")
    void doesNotTriggerWhenAuraItselfIsTargeted() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Crystallization());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Cast Naturalize targeting Crystallization itself (not the enchanted creature)
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, auraPerm.getId());

        // Stack should only have Naturalize, with no Crystallization trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Naturalize");
    }

    @Test
    @DisplayName("Destroying Crystallization in response does not stop its exile trigger")
    void exilesOriginalCreatureAfterAuraIsDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Crystallization());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock(), new Naturalize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, creature.getId());
        assertThat(gd.stack).hasSize(2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Crystallization");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
    }

    @Test
    @DisplayName("An opponent targeting the enchanted creature also triggers exile")
    void exilesCreatureWhenItsControllerTargetsIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Crystallization());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Crystallization")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new Crystallization()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Icy Manipulator");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
