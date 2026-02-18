package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureYouControlTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarkOfDoranTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Bark of Doran has correct card properties")
    void hasCorrectProperties() {
        BarkOfDoran card = new BarkOfDoran();

        assertThat(card.getName()).isEqualTo("Bark of Doran");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getSubtypes()).contains(CardSubtype.EQUIPMENT);
    }

    @Test
    @DisplayName("Bark of Doran has static +0/+1 boost effect")
    void hasStaticBoostEffect() {
        BarkOfDoran card = new BarkOfDoran();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostEquippedCreatureEffect)
                .hasSize(1);
        BoostEquippedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostEquippedCreatureEffect)
                .map(e -> (BoostEquippedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(0);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bark of Doran has assign combat damage with toughness effect")
    void hasAssignCombatDamageWithToughnessEffect() {
        BarkOfDoran card = new BarkOfDoran();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof AssignCombatDamageWithToughnessEffect)
                .hasSize(1);
    }

    @Test
    @DisplayName("Bark of Doran has equip {1} ability with correct properties")
    void hasEquipAbility() {
        BarkOfDoran card = new BarkOfDoran();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(CreatureYouControlTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Bark of Doran and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BarkOfDoran()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bark of Doran")
                        && p.getAttachedTo() == null);
    }

    // ===== Static effects: toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +0/+1")
    void equippedCreatureGetsToughnessBoost() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());

        // GrizzlyBears base 2/2, with Bark becomes 2/3
        assertThat(gs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature loses boost when Bark of Doran is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());

        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(bark);

        assertThat(gs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bark of Doran does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent otherCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());

        assertThat(gs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Combat damage: toughness > power → uses toughness =====

    @Test
    @DisplayName("Unblocked attacker with toughness > power deals toughness as combat damage")
    void unblockedAttackerDealsToughnessAsDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // GrizzlyBears 2/2 + Bark = 2/3, toughness > power → deals 3
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17); // 20 - 3
    }

    @Test
    @DisplayName("getEffectiveCombatDamage returns toughness when toughness > power")
    void effectiveCombatDamageReturnsToughness() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());

        // GrizzlyBears 2/2 + Bark = 2/3, toughness > power
        assertThat(gs.getEffectiveCombatDamage(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocked attacker with toughness > power deals toughness-based damage to blocker")
    void blockedAttackerDealsToughnessToBlocker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // GrizzlyBears 2/2 + Bark = 2/3 attacks, blocked by GrizzlyBears 2/2
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Attacker deals 3 damage (toughness) to blocker with 2 toughness → blocker dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Blocker deals 2 damage to attacker with 3 toughness → attacker survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocker with Bark of Doran deals toughness as combat damage")
    void blockerDealsToughnessAsDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 attacks with HillGiant (3/3)
        Permanent attacker = addReadyCreature(player1, new HillGiant());
        attacker.setAttacking(true);

        // Player2 blocks with GrizzlyBears 2/2 + Bark = 2/3, deals 3 toughness-based damage
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent bark = addBarkReady(player2);
        bark.setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // HillGiant (3/3) takes 3 damage from blocker → dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    // ===== Combat damage: power >= toughness → uses power (normal) =====

    @Test
    @DisplayName("When power equals toughness after boost, creature uses power for combat damage")
    void powerEqualsToughnessUsesPower() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // GoblinPiker 2/1 + Bark = 2/2, toughness equals power → uses power (2)
        Permanent creature = addReadyCreature(player1, new GoblinPiker());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        assertThat(gs.getEffectiveCombatDamage(gd, creature)).isEqualTo(2);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // 20 - 2
    }

    @Test
    @DisplayName("When power > toughness despite boost, creature uses power for combat damage")
    void powerGreaterThanToughnessUsesPower() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // GrizzlyBears 2/2 + Bark (+0/+1) + Warhammer (+3/+0) = 5/3, power > toughness
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature.getId());
        Permanent warhammer = addEquipmentReady(player1, new LoxodonWarhammer());
        warhammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        assertThat(gs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gs.getEffectiveCombatDamage(gd, creature)).isEqualTo(5);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15); // 20 - 5
    }

    // ===== Effect scope: only equipped creature =====

    @Test
    @DisplayName("Unequipped creature uses normal power for combat damage")
    void unequippedCreatureUsesNormalPower() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Bark equipped on creature1, creature2 is unequipped
        Permanent creature1 = addReadyCreature(player1, new GrizzlyBears());
        Permanent bark = addBarkReady(player1);
        bark.setAttachedTo(creature1.getId());

        Permanent creature2 = addReadyCreature(player1, new GrizzlyBears());
        creature2.setAttacking(true);
        // creature1 does NOT attack — only creature2 attacks unblocked

        resolveCombat();

        // creature2 deals normal 2 power damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // 20 - 2
        assertThat(gs.getEffectiveCombatDamage(gd, creature2)).isEqualTo(2);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Bark of Doran can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent bark = addBarkReady(player1);
        Permanent creature1 = addReadyCreature(player1, new GrizzlyBears());
        Permanent creature2 = addReadyCreature(player1, new GrizzlyBears());

        bark.setAttachedTo(creature1.getId());
        assertThat(gs.getEffectiveToughness(gd, creature1)).isEqualTo(3);
        assertThat(gs.getEffectiveCombatDamage(gd, creature1)).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(bark.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses boost and combat damage effect
        assertThat(gs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gs.getEffectiveCombatDamage(gd, creature1)).isEqualTo(2);
        // creature2 gains boost and combat damage effect
        assertThat(gs.getEffectiveToughness(gd, creature2)).isEqualTo(3);
        assertThat(gs.getEffectiveCombatDamage(gd, creature2)).isEqualTo(3);
    }

    // ===== Helpers =====

    private Permanent addBarkReady(Player player) {
        Permanent perm = new Permanent(new BarkOfDoran());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addEquipmentReady(Player player, com.github.laxika.magicalvibes.model.Card equipment) {
        Permanent perm = new Permanent(equipment);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
