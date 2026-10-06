package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletalChangeling.class, WoodlandChangeling.class, ImperiousPerfect.class, NamelessInversion.class})
class SkeletalChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration puts a non-targeting ability on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent perm = addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating regeneration ability consumes {1}{B}")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 1); // needs {1}{B} = 2 mana

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration requires black mana even when enough generic mana is available")
    void cannotActivateWithoutBlackMana() {
        Permanent changeling = addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(changeling.getRegenerationShield()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration shield saves the creature from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent perm = addCreatureReady(player1, new SkeletalChangeling());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new WoodlandChangeling());
        attacker.setAttacking(true);

        resolveCombat(player2);

        Permanent survivor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature dies in combat without a regeneration shield")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent perm = addCreatureReady(player1, new SkeletalChangeling());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new WoodlandChangeling());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Changeling receives an Elf lord's bonus")
    void receivesElfLordBonus() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new SkeletalChangeling());
        harness.addToBattlefield(player1, new ImperiousPerfect());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new SkeletalChangeling());
        changeling.setSummoningSick(true);
        changeling.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getRegenerationShield()).isEqualTo(1);
        assertThat(changeling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creating a shield does not tap the creature or regenerate it immediately")
    void shieldDoesNotImmediatelyRegenerate() {
        Permanent changeling = addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.isTapped()).isFalse();
        assertThat(changeling.getTimesRegeneratedThisTurn()).isZero();
        assertThat(changeling.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations create independent shields and combat consumes only one")
    void combatConsumesOnlyOneShield() {
        Permanent changeling = addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(changeling.getRegenerationShield()).isEqualTo(2);

        changeling.setBlocking(true);
        changeling.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new WoodlandChangeling());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Skeletal Changeling");
        assertThat(changeling.getRegenerationShield()).isEqualTo(1);
        assertThat(changeling.isTapped()).isTrue();
        assertThat(changeling.isBlocking()).isFalse();
        assertThat(changeling.getBlockingTargets()).isEmpty();
        assertThat(changeling.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Regeneration cannot save a creature with zero or negative toughness")
    void regenerationDoesNotSaveFromNegativeToughness() {
        Permanent changeling = addCreatureReady(player1, new SkeletalChangeling());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, changeling.getId());

        harness.assertNotOnBattlefield(player1, "Skeletal Changeling");
        harness.assertInGraveyard(player1, "Skeletal Changeling");
    }
}
