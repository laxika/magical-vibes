package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

class ExecutionersHoodTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Executioner's Hood has static intimidate keyword grant effect")
    void hasKeywordGrantEffect() {
        ExecutionersHood card = new ExecutionersHood();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.scope() == GrantScope.EQUIPPED_CREATURE)
                .toList();
        assertThat(keywordEffects).hasSize(1);
        assertThat(keywordEffects.getFirst().keywords()).containsExactly(Keyword.INTIMIDATE);
    }

    @Test
    @DisplayName("Executioner's Hood has equip {2} ability with correct properties")
    void hasEquipAbility() {
        ExecutionersHood card = new ExecutionersHood();

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
    @DisplayName("Casting Executioner's Hood and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ExecutionersHood()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Executioner's Hood")
                        && !p.isAttached());
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Executioner's Hood to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent hood = addHoodReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hood.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: keyword grants =====

    @Test
    @DisplayName("Equipped creature has intimidate")
    void equippedCreatureHasIntimidate() {
        Permanent creature = addReadyCreature(player1);
        Permanent hood = addHoodReady(player1);
        hood.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses intimidate when Executioner's Hood is removed")
    void creatureLosesIntimidateWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent hood = addHoodReady(player1);
        hood.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(hood);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Executioner's Hood does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent hood = addHoodReady(player1);
        hood.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.INTIMIDATE)).isFalse();
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Executioner's Hood can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent hood = addHoodReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        hood.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INTIMIDATE)).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(hood.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INTIMIDATE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INTIMIDATE)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addHoodReady(Player player) {
        Permanent perm = new Permanent(new ExecutionersHood());
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
