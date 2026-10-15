package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({IceCage.class, GrizzlyBears.class, BottleGnomes.class, Naturalize.class,
        Shock.class, IcyManipulator.class, LlanowarElves.class})
class IceCageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Ice Cage attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new IceCage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ice Cage")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Ice Caged creature cannot attack")
    void iceCagedCreatureCannotAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player2, new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ice Caged creature cannot block")
    void iceCagedCreatureCannotBlock() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player1, new IceCage());
        iceCagePerm.setAttachedTo(blockerPerm.getId());

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
    @DisplayName("Ice Caged creature cannot activate abilities")
    void iceCagedCreatureCannotActivateAbilities() {
        Permanent gnomesPerm = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        gnomesPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player2, new IceCage());
        iceCagePerm.setAttachedTo(gnomesPerm.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Ice Cage is destroyed when enchanted creature becomes target of a spell")
    void destroyedWhenEnchantedCreatureTargetedBySpell() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player1, new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());

        // Cast Shock targeting the Ice Caged creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsPerm.getId());

        // Stack should have Shock + Ice Cage's triggered ability on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Ice Cage's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Ice Cage should be destroyed
        harness.assertNotOnBattlefield(player1, "Ice Cage");
        harness.assertInGraveyard(player1, "Ice Cage");
    }

    @Test
    @DisplayName("Ice Cage is destroyed when enchanted creature becomes target of an ability")
    void destroyedWhenEnchantedCreatureTargetedByAbility() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player1, new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());

        // Use Icy Manipulator to target the Ice Caged creature with an activated ability
        Permanent icyPerm = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        icyPerm.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(icyPerm), null, bearsPerm.getId());

        // Stack should have Icy Manipulator's ability + Ice Cage's triggered ability on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Ice Cage's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Ice Cage should be destroyed
        harness.assertNotOnBattlefield(player1, "Ice Cage");
        harness.assertInGraveyard(player1, "Ice Cage");
        // "Destroy this Aura" destroys the Aura only — the creature it cages is released, not killed.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature can attack after Ice Cage is destroyed by targeting")
    void creatureCanAttackAfterIceCageDestroyed() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player2, new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());

        // Cannot attack while caged
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        // Remove Ice Cage (simulating destruction)
        gd.playerBattlefields.get(player2.getId()).remove(iceCagePerm);

        // Now creature can attack
        harness.beginAttackerDeclarationInput();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Targeting Ice Cage itself does not trigger its destruction")
    void doesNotTriggerWhenIceCageItselfIsTargeted() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent iceCagePerm = harness.addToBattlefieldAndReturn(player1, new IceCage());
        iceCagePerm.setAttachedTo(bearsPerm.getId());

        // Cast Naturalize targeting Ice Cage itself (not the enchanted creature)
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, iceCagePerm.getId());

        // Stack should only have Naturalize — no Ice Cage trigger
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Naturalize");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Ice Cage")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new IceCage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Icy Manipulator");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ice Cage prevents mana abilities without paying their tap cost")
    void preventsManaAbilities() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);
        Permanent cage = harness.addToBattlefieldAndReturn(player2, new IceCage());
        cage.setAttachedTo(elves.getId());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(elves.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Ice Cage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting another creature does not destroy Ice Cage")
    void targetingAnotherCreatureDoesNotTrigger() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent cage = harness.addToBattlefieldAndReturn(player1, new IceCage());
        cage.setAttachedTo(enchanted.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, other.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ice Cage");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted).doesNotContain(other);
    }

    @Test
    @DisplayName("The creature's controller can target it and activate abilities after Ice Cage is destroyed")
    void creatureControllerCanReleaseActivatedAbilities() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player2, new BottleGnomes());
        Permanent cage = harness.addToBattlefieldAndReturn(player1, new IceCage());
        cage.setAttachedTo(gnomes.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0, gnomes.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());
        harness.assertOnBattlefield(player1, "Ice Cage");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Ice Cage");
        assertThat(gd.stack).hasSize(1);
        assertThat(gnomes.getMarkedDamage()).isZero();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bottle Gnomes");
        harness.assertLife(player2, 23);
        harness.passBothPriorities();
        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Aura spell destroys the existing Ice Cage before attaching")
    void anotherAuraSpellTriggersExistingCage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstCage = harness.addToBattlefieldAndReturn(player1, new IceCage());
        firstCage.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new IceCage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ice Cage");
        harness.assertInGraveyard(player1, "Ice Cage");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(aura -> {
                    assertThat(aura.getId()).isNotEqualTo(firstCage.getId());
                    assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
                });
    }
}
