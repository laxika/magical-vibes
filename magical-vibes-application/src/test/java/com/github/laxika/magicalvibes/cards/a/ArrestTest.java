package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BargainingTable;
import com.github.laxika.magicalvibes.cards.c.CrossbowInfantry;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.v.VineTrellis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Arrest.class, ArmsDealer.class, BargainingTable.class, CrossbowInfantry.class,
        FreshVolunteers.class, VineTrellis.class})
class ArrestTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Arrest puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player2, new FreshVolunteers());

        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Arrest attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player2, new FreshVolunteers());

        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent arrest = findPermanent(player1, "Arrest");
        assertThat(arrest.isAttached()).isTrue();
        assertThat(arrest.getAttachedTo()).isEqualTo(bearsPerm.getId());
    }

    // ===== Prevents attacking =====

    @Test
    @DisplayName("Arrested creature cannot attack")
    void arrestedCreatureCannotAttack() {
        Permanent bearsPerm = addCreatureReady(player1, new FreshVolunteers());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Prevents blocking =====

    @Test
    @DisplayName("Arrested creature cannot block")
    void arrestedCreatureCannotBlock() {
        Permanent blockerPerm = addCreatureReady(player2, new FreshVolunteers());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(blockerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(arrestPerm);

        Permanent atkPerm = addCreatureReady(player1, new FreshVolunteers());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    // ===== Prevents activated abilities =====

    @Test
    @DisplayName("Arrested creature cannot activate abilities")
    void arrestedCreatureCannotActivateAbilities() {
        Permanent dealerPerm = addCreatureReady(player1, new ArmsDealer());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(dealerPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Arrested creature with tap ability cannot activate it")
    void arrestedCreatureCannotActivateTapAbility() {
        Permanent infantryPerm = addCreatureReady(player1, new CrossbowInfantry());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(infantryPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        Permanent targetPerm = addCreatureReady(player2, new FreshVolunteers());
        targetPerm.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Arrested creature cannot activate a mana ability")
    void arrestedCreatureCannotActivateManaAbility() {
        Permanent trellisPerm = addCreatureReady(player1, new VineTrellis());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(trellisPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Removal restores abilities =====

    @Test
    @DisplayName("Creature can activate abilities again after Arrest is removed")
    void creatureCanActivateAfterArrestRemoved() {
        Permanent infantryPerm = addCreatureReady(player1, new CrossbowInfantry());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(infantryPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        Permanent targetPerm = addCreatureReady(player2, new FreshVolunteers());
        targetPerm.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player2.getId()).remove(arrestPerm);

        harness.activateAbility(player1, 0, null, targetPerm.getId());
        harness.passBothPriorities();

        assertThat(infantryPerm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("deals 1 damage")).isTrue();
    }

    @Test
    @DisplayName("Creature can attack again after Arrest is removed")
    void creatureCanAttackAfterArrestRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new FreshVolunteers());

        Permanent arrestPerm = new Permanent(new Arrest());
        arrestPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(arrestPerm);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player2.getId()).remove(arrestPerm);

        declareAttackers(List.of(0));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Arrest")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Arrest")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BargainingTable());
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Arrest fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent targetCreature = addCreatureReady(player2, new FreshVolunteers());

        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, targetCreature.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arrest");
        harness.assertNotOnBattlefield(player1, "Arrest");
    }
}
