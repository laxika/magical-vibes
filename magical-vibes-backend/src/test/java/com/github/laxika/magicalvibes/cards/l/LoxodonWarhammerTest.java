package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureYouControlTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoxodonWarhammerTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Loxodon Warhammer has correct card properties")
    void hasCorrectProperties() {
        LoxodonWarhammer card = new LoxodonWarhammer();

        assertThat(card.getName()).isEqualTo("Loxodon Warhammer");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{3}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getSubtypes()).contains(CardSubtype.EQUIPMENT);
    }

    @Test
    @DisplayName("Loxodon Warhammer has static +3/+0 boost effect")
    void hasStaticBoostEffect() {
        LoxodonWarhammer card = new LoxodonWarhammer();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostEquippedCreatureEffect)
                .hasSize(1);
        BoostEquippedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostEquippedCreatureEffect)
                .map(e -> (BoostEquippedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Loxodon Warhammer has static trample and lifelink keyword grant effects")
    void hasKeywordGrantEffects() {
        LoxodonWarhammer card = new LoxodonWarhammer();

        List<GrantKeywordToEquippedCreatureEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordToEquippedCreatureEffect)
                .map(e -> (GrantKeywordToEquippedCreatureEffect) e)
                .toList();
        assertThat(keywordEffects).hasSize(2);
        assertThat(keywordEffects).extracting(GrantKeywordToEquippedCreatureEffect::keyword)
                .containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Loxodon Warhammer has equip {3} ability with correct properties")
    void hasEquipAbility() {
        LoxodonWarhammer card = new LoxodonWarhammer();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
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
    @DisplayName("Casting Loxodon Warhammer and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LoxodonWarhammer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Loxodon Warhammer")
                        && p.getAttachedTo() == null);
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Warhammer to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(warhammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature loses boost when Warhammer is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
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
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Static effects: keyword grants =====

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has lifelink")
    void equippedCreatureHasLifelink() {
        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Creature loses trample and lifelink when Warhammer is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
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

        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 5 power (2 base + 3 from Warhammer)
        // Player2 takes 5 damage: 20 - 5 = 15
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Player1 gains 5 life from lifelink: 20 + 5 = 25
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    // ===== Lifelink: blocked combat damage =====

    @Test
    @DisplayName("Controller gains life when equipped creature deals combat damage to blocker")
    void lifelinkGainsLifeOnCombatDamageToBlocker() {
        harness.setLife(player1, 20);

        // 2/2 with Warhammer (5/2) attacks, blocked by 2/2
        Permanent attacker = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Attacker assigns 2 lethal to blocker + 3 tramples to player = 5 total damage dealt
        // Player1 gains 5 life from lifelink: 20 + 5 = 25
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    // ===== Lifelink: trample damage =====

    @Test
    @DisplayName("Lifelink applies to total damage including trample overflow")
    void lifelinkAppliesToTrampleDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // 2/2 with Warhammer (5/2, trample) attacks, blocked by 2/2
        Permanent attacker = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Attacker has 5 power, blocker has 2 toughness
        // 2 damage to blocker, 3 tramples to player2 (20 - 3 = 17)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Player1 gains 5 total from lifelink (2 to blocker + 3 to player): 20 + 5 = 25
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    // ===== Lifelink: blocker with equipment =====

    @Test
    @DisplayName("Blocking creature with Warhammer gains life for its controller")
    void lifelinkOnBlocker() {
        harness.setLife(player2, 20);

        // Player1 attacks with 2/2
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        // Player2 blocks with 2/2 equipped with Warhammer (5/2)
        Permanent blocker = addReadyCreature(player2);
        Permanent warhammer = addWarhammerReady(player2);
        warhammer.setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Blocker dealt 5 damage to attacker → player2 gains 5 life: 20 + 5 = 25
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(25);
    }

    // ===== Lifelink: no damage, no life gain =====

    @Test
    @DisplayName("No lifelink life gain when equipped creature does not deal damage")
    void noLifelinkWhenNoDamageDealt() {
        harness.setLife(player1, 20);

        // Creature with Warhammer does not attack
        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        // Another creature attacks unblocked
        Permanent otherAttacker = addReadyCreature(player1);
        otherAttacker.setAttacking(true);

        resolveCombat();

        // Player1 gains no lifelink life — equipped creature didn't deal damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Lifelink: logging =====

    @Test
    @DisplayName("Lifelink life gain is logged")
    void lifelinkLifeGainIsLogged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains") && log.contains("life") && log.contains("lifelink"));
    }

    // ===== Lifelink + Spirit Link stacking =====

    @Test
    @DisplayName("Lifelink and Spirit Link both trigger, granting life separately")
    void lifelinkAndSpiritLinkStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Creature with Warhammer (lifelink) AND Spirit Link aura
        Permanent creature = addReadyCreature(player1);
        Permanent warhammer = addWarhammerReady(player1);
        warhammer.setAttachedTo(creature.getId());

        com.github.laxika.magicalvibes.cards.s.SpiritLink spiritLink = new com.github.laxika.magicalvibes.cards.s.SpiritLink();
        Permanent aura = new Permanent(spiritLink);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        creature.setAttacking(true);

        resolveCombat();

        // Creature power is 5 (2 + 3)
        // Player2 takes 5 damage: 20 - 5 = 15
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Player1 gains 5 from lifelink + 5 from Spirit Link = 10 total: 20 + 10 = 30
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Warhammer can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent warhammer = addWarhammerReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

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
        Permanent perm = new Permanent(new LoxodonWarhammer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
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
