package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoxodonWarhammer.class, GrizzlyBears.class, SpiritLink.class})
class LoxodonWarhammerTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Loxodon Warhammer and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LoxodonWarhammer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Loxodon Warhammer")
                        && !p.isAttached());
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Equip ability requires three mana")
    void equipRequiresThreeMana() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(warhammer.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip ability can target only a creature controlled by its controller")
    void cannotEquipOpponentsCreature() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(warhammer.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip ability can be activated only at sorcery speed")
    void cannotEquipOutsideSorcerySpeed() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(warhammer.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Resolving equip ability attaches Warhammer to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(warhammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Successful equip is not logged as a fizzle")
    void successfulEquipIsNotLoggedAsFizzle() {
        addWarhammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Loxodon Warhammer") && log.contains("fizzles"));
    }

    @Test
    @DisplayName("Equip fizzles if the target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(warhammer.getAttachedTo()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Loxodon Warhammer") && log.contains("fizzles"));
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature loses boost when Warhammer is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(warhammer);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Warhammer does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Static effects: keyword grants =====

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has lifelink")
    void equippedCreatureHasLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses trample and lifelink when Warhammer is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(warhammer);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    // ===== Lifelink: unblocked combat damage =====

    @Test
    @DisplayName("Controller gains life when equipped creature deals combat damage to player")
    void lifelinkGainsLifeOnCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 5 power (2 base + 3 from Warhammer)
        // Player2 takes 5 damage: 20 - 5 = 15
        harness.assertLife(player2, 15);
        // Player1 gains 5 life from lifelink: 20 + 5 = 25
        harness.assertLife(player1, 25);
    }

    // ===== Lifelink: blocked combat damage =====

    @Test
    @DisplayName("Controller gains life when equipped creature deals combat damage to blocker")
    void lifelinkGainsLifeOnCombatDamageToBlocker() {
        harness.setLife(player1, 20);

        // 2/2 with Warhammer (5/2) attacks, blocked by 2/2
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Trample creature blocked → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        // Attacker assigns 2 lethal to blocker + 3 tramples to player = 5 total damage dealt
        // Player1 gains 5 life from lifelink: 20 + 5 = 25
        harness.assertLife(player1, 25);
    }

    // ===== Lifelink: trample damage =====

    @Test
    @DisplayName("Lifelink applies to total damage including trample overflow")
    void lifelinkAppliesToTrampleDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // 2/2 with Warhammer (5/2, trample) attacks, blocked by 2/2
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Trample creature blocked → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        // Attacker has 5 power, blocker has 2 toughness
        // 2 damage to blocker, 3 tramples to player2 (20 - 3 = 17)
        harness.assertLife(player2, 17);
        // Player1 gains 5 total from lifelink (2 to blocker + 3 to player): 20 + 5 = 25
        harness.assertLife(player1, 25);
    }

    // ===== Lifelink: blocker with equipment =====

    @Test
    @DisplayName("Blocking creature with Warhammer gains life for its controller")
    void lifelinkOnBlocker() {
        harness.setLife(player2, 20);

        // Player1 attacks with 2/2
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // Player2 blocks with 2/2 equipped with Warhammer (5/2)
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player2);
        warhammer.setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Blocker dealt 5 damage to attacker → player2 gains 5 life: 20 + 5 = 25
        harness.assertLife(player2, 25);
    }

    // ===== Lifelink: no damage, no life gain =====

    @Test
    @DisplayName("No lifelink life gain when equipped creature does not deal damage")
    void noLifelinkWhenNoDamageDealt() {
        harness.setLife(player1, 20);

        // Creature with Warhammer does not attack
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        // Another creature attacks unblocked
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        otherAttacker.setAttacking(true);

        resolveCombat();

        // Player1 gains no lifelink life — equipped creature didn't deal damage
        harness.assertLife(player1, 20);
    }

    // ===== Lifelink: logging =====

    @Test
    @DisplayName("Lifelink life gain is logged")
    void lifelinkLifeGainIsLogged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains") && log.contains("life") && log.contains("lifelink"));
    }

    // ===== Lifelink + Spirit Link stacking =====

    @Test
    @DisplayName("Lifelink and Spirit Link both trigger, granting life separately")
    void lifelinkAndSpiritLinkStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Creature with Warhammer (lifelink) AND Spirit Link aura
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiritLink());
        aura.setAttachedTo(creature.getId());

        creature.setAttacking(true);

        resolveCombat();

        // Creature power is 5 (2 + 3)
        // Player2 takes 5 damage: 20 - 5 = 15
        harness.assertLife(player2, 15);
        // Player1 gains 5 from lifelink + 5 from Spirit Link = 10 total: 20 + 10 = 30
        harness.assertLife(player1, 30);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Warhammer can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        warhammer.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.LIFELINK)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(warhammer.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.LIFELINK)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.LIFELINK)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addWarhammerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LoxodonWarhammer());
        perm.setSummoningSick(false);
        return perm;
    }
}

