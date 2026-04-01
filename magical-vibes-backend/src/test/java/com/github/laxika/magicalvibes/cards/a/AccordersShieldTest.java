package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class AccordersShieldTest {

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
    @DisplayName("Accorder's Shield has static +0/+3 boost effect")
    void hasStaticBoostEffect() {
        AccordersShield card = new AccordersShield();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof StaticBoostEffect)
                .hasSize(1);
        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(0);
        assertThat(boost.toughnessBoost()).isEqualTo(3);
    }

    @Test
    @DisplayName("Accorder's Shield has static vigilance keyword grant effect")
    void hasVigilanceGrantEffect() {
        AccordersShield card = new AccordersShield();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.scope() == GrantScope.EQUIPPED_CREATURE)
                .toList();
        assertThat(keywordEffects).hasSize(1);
        assertThat(keywordEffects.getFirst().keywords()).containsExactly(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Accorder's Shield has equip {3} ability with correct properties")
    void hasEquipAbility() {
        AccordersShield card = new AccordersShield();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Accorder's Shield for {0} and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Accorder's Shield")
                        && !p.isAttached());
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Shield to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +0/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);    // 2 + 0
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5); // 2 + 3
    }

    @Test
    @DisplayName("Equipped creature loses boost when Shield is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shield does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Static effects: vigilance keyword grant =====

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses vigilance when Shield is removed")
    void creatureLosesVigilanceWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Vigilance: creature doesn't tap when attacking =====

    @Test
    @DisplayName("Equipped creature with vigilance does not tap when attacking")
    void equippedCreatureDoesNotTapWhenAttacking() {
        Permanent creature = addReadyCreature(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        creature.setAttacking(true);

        // With vigilance, the creature should not be tapped
        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Shield can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent shield = addShieldReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        shield.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.VIGILANCE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.VIGILANCE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Equip fizzle =====

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target creature before resolution
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Equipment should still be on battlefield, unattached
        assertThat(gd.stack).isEmpty();
        Permanent remaining = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Accorder's Shield"))
                .findFirst().orElseThrow();
        assertThat(remaining.getAttachedTo()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addShieldReady(Player player) {
        Permanent perm = new Permanent(new AccordersShield());
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
}
