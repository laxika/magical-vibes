package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrossbowInfantry;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.k.KyrenToy;
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

@CardUsed({Muzzle.class, CrossbowInfantry.class, Disenchant.class, JhovallQueen.class, KyrenToy.class})
class MuzzleTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Muzzle")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new JhovallQueen());
        harness.setHand(player1, List.of(new Muzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Muzzle attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new JhovallQueen());
        harness.setHand(player1, List.of(new Muzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Muzzle
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Muzzle")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new KyrenToy());
        harness.setHand(player1, List.of(new Muzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted attacker deals no combat damage")
    void enchantedAttackerDealsNoCombatDamage() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new JhovallQueen());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Muzzle());
        aura.setAttachedTo(attacker.getId());

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage dealt to the enchanted creature is not prevented")
    void damageToEnchantedCreatureStillApplies() {
        Permanent source = addCreatureReady(player1, new CrossbowInfantry());
        Permanent creature = addCreatureReady(player2, new JhovallQueen());
        creature.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Muzzle());
        aura.setAttachedTo(creature.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Jhovall Queen");
    }

    @Test
    @DisplayName("Enchanted creature deals no noncombat damage")
    void enchantedCreatureDealsNoNoncombatDamage() {
        Permanent source = addCreatureReady(player1, new CrossbowInfantry());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Muzzle());
        aura.setAttachedTo(source.getId());

        Permanent target = addCreatureReady(player2, new JhovallQueen());
        target.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Jhovall Queen");
    }

    @Test
    @DisplayName("An opponent's Muzzle prevents an attacker's damage to a blocker, but not incoming damage")
    void enchantedAttackerDealsNoDamageToBlocker() {
        Permanent attacker = addCreatureReady(player1, new JhovallQueen());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new JhovallQueen());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Muzzle());
        aura.setAttachedTo(attacker.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An enchanted blocker deals no combat damage but still takes damage")
    void enchantedBlockerDealsNoCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new JhovallQueen());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new JhovallQueen());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Muzzle());
        aura.setAttachedTo(blocker.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing Muzzle in response lets the enchanted creature's pending ability deal damage")
    void removingMuzzleBeforeDamageAbilityResolvesRestoresDamage() {
        Permanent source = addCreatureReady(player1, new CrossbowInfantry());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Muzzle());
        aura.setAttachedTo(source.getId());
        Permanent target = addCreatureReady(player2, new JhovallQueen());
        target.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null,
                target.getId());
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Muzzle");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }
}
