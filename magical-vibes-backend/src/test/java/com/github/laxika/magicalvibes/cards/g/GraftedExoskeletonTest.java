package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraftedExoskeletonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Grafted Exoskeleton has static +2/+2 boost effect")
    void hasStaticBoostEffect() {
        GraftedExoskeleton card = new GraftedExoskeleton();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostAttachedCreatureEffect)
                .hasSize(1);
        BoostAttachedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostAttachedCreatureEffect)
                .map(e -> (BoostAttachedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("Grafted Exoskeleton grants infect to equipped creature")
    void hasInfectGrantEffect() {
        GraftedExoskeleton card = new GraftedExoskeleton();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.scope() == GrantScope.EQUIPPED_CREATURE)
                .toList();
        assertThat(keywordEffects).hasSize(1);
        assertThat(keywordEffects.getFirst().keyword()).isEqualTo(Keyword.INFECT);
    }

    @Test
    @DisplayName("Grafted Exoskeleton has sacrifice-on-unattach effect")
    void hasSacrificeOnUnattachEffect() {
        GraftedExoskeleton card = new GraftedExoskeleton();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof SacrificeOnUnattachEffect)
                .hasSize(1);
    }

    @Test
    @DisplayName("Grafted Exoskeleton has equip {2} ability")
    void hasEquipAbility() {
        GraftedExoskeleton card = new GraftedExoskeleton();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().getFirst().getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Grafted Exoskeleton puts it on the battlefield unattached")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GraftedExoskeleton()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grafted Exoskeleton")
                        && !p.isAttached());
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Exoskeleton to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent exoskeleton = addExoskeletonReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(exoskeleton.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent exoskeleton = addExoskeletonReady(player1);
        exoskeleton.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    // ===== Static effects: infect keyword =====

    @Test
    @DisplayName("Equipped creature has infect")
    void equippedCreatureHasInfect() {
        Permanent creature = addReadyCreature(player1);
        Permanent exoskeleton = addExoskeletonReady(player1);
        exoskeleton.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Creature loses infect when Exoskeleton is removed")
    void creatureLosesInfectWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent exoskeleton = addExoskeletonReady(player1);
        exoskeleton.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(exoskeleton);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
    }

    // ===== Infect: combat damage =====

    @Test
    @DisplayName("Equipped creature deals combat damage as poison counters to defending player")
    void infectDealsPoisonCountersToPlayer() {
        Permanent creature = addReadyCreature(player1);
        Permanent exoskeleton = addExoskeletonReady(player1);
        exoskeleton.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 4 power (2 + 2), infect deals poison counters
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        // Player2 life should be unchanged (infect deals poison, not damage)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Equipped creature deals combat damage as -1/-1 counters to blocking creature")
    void infectDealsMinusCountersToCreature() {
        Permanent attacker = addReadyCreature(player1);
        Permanent exoskeleton = addExoskeletonReady(player1);
        exoskeleton.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // 2/2 blocker
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Attacker has 4 power with infect → puts 4 -1/-1 counters on blocker
        // Blocker was 2/2, now -2/-2 toughness → dead (SBA)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    // ===== Sacrifice on unattach: re-equip =====

    @Test
    @DisplayName("Re-equipping Grafted Exoskeleton sacrifices the previously equipped creature")
    void reEquipSacrificesPreviousCreature() {
        Permanent exoskeleton = addExoskeletonReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        // Attach to creature1 first
        exoskeleton.setAttachedTo(creature1.getId());

        // Re-equip to creature2
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        // Exoskeleton now on creature2
        assertThat(exoskeleton.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 was sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        // creature1 went to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // creature2 is still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature2.getId()));
    }

    // ===== Sacrifice on unattach: equipment destroyed =====

    @Test
    @DisplayName("Destroying Grafted Exoskeleton sacrifices the equipped creature")
    void destroyingExoskeletonSacrificesCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent exoskeleton = addExoskeletonReady(player1);
        exoskeleton.setAttachedTo(creature.getId());

        // Player1 casts something so player2 can respond with Naturalize
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, exoskeleton.getId());
        harness.passBothPriorities(); // Naturalize resolves
        harness.passBothPriorities(); // Grizzly Bears resolves

        // Exoskeleton is destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grafted Exoskeleton"));
        // Creature is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        // Both went to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grafted Exoskeleton"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Sacrifice on unattach: no sacrifice when equipping from unattached =====

    @Test
    @DisplayName("Equipping from unattached state does not sacrifice anything")
    void equippingFromUnattachedDoesNotSacrifice() {
        Permanent exoskeleton = addExoskeletonReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        // Exoskeleton is unattached, equip to creature1
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature1.getId());
        harness.passBothPriorities();

        // Both creatures still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature1.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature2.getId()));
        // No creatures in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Sacrifice on unattach: re-equip to same creature =====

    @Test
    @DisplayName("Re-equipping to same creature does not sacrifice it")
    void reEquipToSameCreatureDoesNotSacrifice() {
        Permanent exoskeleton = addExoskeletonReady(player1);
        Permanent creature = addReadyCreature(player1);

        exoskeleton.setAttachedTo(creature.getId());

        // Re-equip to same creature
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Creature is still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    // ===== Helpers =====

    private Permanent addExoskeletonReady(Player player) {
        Permanent perm = new Permanent(new GraftedExoskeleton());
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
