package com.github.laxika.magicalvibes.cards.s;

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
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StriderHarnessTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Strider Harness has static +1/+1 boost effect")
    void hasStaticBoostEffect() {
        StriderHarness card = new StriderHarness();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof BoostAttachedCreatureEffect)
                .hasSize(1);
        BoostAttachedCreatureEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof BoostAttachedCreatureEffect)
                .map(e -> (BoostAttachedCreatureEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("Strider Harness has static haste keyword grant effect")
    void hasHasteGrantEffect() {
        StriderHarness card = new StriderHarness();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.scope() == GrantScope.EQUIPPED_CREATURE)
                .toList();
        assertThat(keywordEffects).hasSize(1);
        assertThat(keywordEffects.getFirst().keyword()).isEqualTo(Keyword.HASTE);
    }

    @Test
    @DisplayName("Strider Harness has equip {1} ability with correct properties")
    void hasEquipAbility() {
        StriderHarness card = new StriderHarness();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
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
    @DisplayName("Casting Strider Harness and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new StriderHarness()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Strider Harness")
                        && p.getAttachedTo() == null);
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Strider Harness to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent harnessPerm = addHarnessReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(harnessPerm.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: power/toughness boost =====

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Equipped creature loses boost when Strider Harness is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(harnessPerm);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Strider Harness does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Static effects: keyword grants =====

    @Test
    @DisplayName("Equipped creature has haste")
    void equippedCreatureHasHaste() {
        Permanent creature = addReadyCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses haste when Strider Harness is removed")
    void creatureLosesHasteWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(harnessPerm);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    // ===== Haste: summoning sickness bypass =====

    @Test
    @DisplayName("Summoning-sick creature with Strider Harness can attack")
    void summoningSickCreatureCanAttackWithHaste() {
        Permanent creature = addSummoningSickCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 3 power (2 + 1 from harness), deals damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Strider Harness can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent harnessPerm = addHarnessReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        harnessPerm.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HASTE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(harnessPerm.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HASTE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.HASTE)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addHarnessReady(Player player) {
        Permanent perm = new Permanent(new StriderHarness());
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

    private Permanent addSummoningSickCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(true);
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
