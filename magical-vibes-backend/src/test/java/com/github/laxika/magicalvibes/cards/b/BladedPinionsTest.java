package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
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

class BladedPinionsTest extends BaseCardTest {


    // ===== Card properties =====


    @Test
    @DisplayName("Bladed Pinions has static flying and first strike keyword grant effects")
    void hasKeywordGrantEffects() {
        BladedPinions card = new BladedPinions();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.scope() == GrantScope.EQUIPPED_CREATURE)
                .toList();
        assertThat(keywordEffects).hasSize(2);
        assertThat(keywordEffects).extracting(GrantKeywordEffect::keyword)
                .containsExactlyInAnyOrder(Keyword.FLYING, Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Bladed Pinions has equip {2} ability with correct properties")
    void hasEquipAbility() {
        BladedPinions card = new BladedPinions();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
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
    @DisplayName("Casting Bladed Pinions and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BladedPinions()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bladed Pinions")
                        && !p.isAttached());
    }

    // ===== Equip ability: resolving =====

    @Test
    @DisplayName("Resolving equip ability attaches Bladed Pinions to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    // ===== Static effects: keyword grants =====

    @Test
    @DisplayName("Equipped creature has flying")
    void equippedCreatureHasFlying() {
        Permanent creature = addReadyCreature(player1);
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has first strike")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addReadyCreature(player1);
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses flying and first strike when Bladed Pinions is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(pinions);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bladed Pinions does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Bladed Pinions can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent pinions = addPinionsReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        pinions.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(pinions.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses keywords
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        // creature2 gains keywords
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("Equipped creature with first strike kills blocker before regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // 2/2 with Bladed Pinions (flying, first strike) attacks
        Permanent attacker = addReadyCreature(player1);
        Permanent pinions = addPinionsReady(player1);
        pinions.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        // Blocked by 2/2 (no first strike)
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // First strike 2/2 deals 2 damage to blocker first, killing it
        // Blocker (2 toughness) is destroyed before dealing regular damage
        // Attacker survives since blocker is dead before regular damage step
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    // ===== Helpers =====

    private Permanent addPinionsReady(Player player) {
        Permanent perm = new Permanent(new BladedPinions());
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
