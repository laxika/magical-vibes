package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindControl.class, RuneclawBear.class, Naturalize.class, Spellbook.class})
class MindControlTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Mind Control targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mind Control");
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }


    @Test
    @DisplayName("Resolving Mind Control steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Mind Control aura should be on player1's battlefield attached to the creature
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mind Control")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));

        // Stolen creature should be summoning sick
        assertThat(creature.isSummoningSick()).isTrue();

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("Mind Control fizzles if target creature is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // Mind Control should be in graveyard
        harness.assertInGraveyard(player1, "Mind Control");
    }

    @Test
    @DisplayName("Creature returns to owner when Mind Control is destroyed")
    void creatureReturnsWhenMindControlDestroyed() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        // Player1 casts Mind Control, resolve it
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Find the Mind Control aura permanent
        Permanent mindControlPerm = findPermanent(player1, "Mind Control");

        // Set up for Naturalize: force step to a main phase, give player2 priority
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        // Player1 passes, player2 casts Naturalize targeting Mind Control
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mindControlPerm.getId());

        // Creature should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Stolen creatures map should be cleaned up
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }


    @Test
    @DisplayName("Can target a creature with Mind Control")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Mind Control")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        Permanent artifact = findPermanent(player1, "Spellbook");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting your own creature does not change its control or tap state")
    void enchantingOwnCreatureKeepsControl() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Runeclaw Bear").getId()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isSummoningSick()).isFalse();
        assertThat(findPermanent(player1, "Mind Control").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Removing the newer Mind Control restores the older Aura's controller")
    void removingNewerControlRestoresOlderControl() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new MindControl()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MindControl()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Runeclaw Bear").getId()).isEqualTo(creature.getId());
        Permanent newerAura = findPermanent(player2, "Mind Control");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, newerAura.getId());

        assertThat(findPermanent(player1, "Runeclaw Bear").getId()).isEqualTo(creature.getId());
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(findPermanent(player1, "Mind Control").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Mind Control");
    }
}
