package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndestructibilityTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Indestructibility has static grant indestructible effect")
    void hasCorrectProperties() {
        Indestructibility card = new Indestructibility();

        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(grant.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(grant.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);
    }

    @Test
    @DisplayName("Indestructibility has no target filter (enchant any permanent)")
    void hasNoTargetFilter() {
        Indestructibility card = new Indestructibility();

        assertThat(card.getTargetFilter()).isNull();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Indestructibility targeting a creature puts it on the stack")
    void castingOnCreaturePutsOnStack() {
        Permanent creature = addReadyCreature(player1);
        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Indestructibility");
    }

    @Test
    @DisplayName("Resolving Indestructibility attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addReadyCreature(player1);
        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Indestructibility")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can cast Indestructibility targeting a noncreature permanent")
    void canTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        harness.setHand(player1, List.of(new Indestructibility()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Indestructible granted to enchanted permanent =====

    @Test
    @DisplayName("Enchanted creature has indestructible")
    void enchantedCreatureHasIndestructible() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = addAttachedAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted noncreature permanent has indestructible")
    void enchantedNonCreatureHasIndestructible() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        Permanent aura = new Permanent(new Indestructibility());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Indestructible prevents destruction =====

    @Test
    @DisplayName("Enchanted creature survives destroy effect")
    void enchantedCreatureSurvivesDestroy() {
        Permanent creature = addReadyCreature(player1);
        addAttachedAura(player1, creature);

        // Cast Doom Blade targeting the indestructible creature
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Enchanted creature survives lethal damage")
    void enchantedCreatureSurvivesLethalDamage() {
        Permanent creature = addReadyCreature(player1);
        addAttachedAura(player1, creature);

        // Mark 10 damage on the 2/2 creature
        creature.setMarkedDamage(10);
        harness.passBothPriorities();

        // Creature should survive because it's indestructible
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Permanent loses indestructible when Indestructibility is removed")
    void losesIndestructibleWhenAuraRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent aura = addAttachedAura(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Does not affect other permanents =====

    @Test
    @DisplayName("Indestructibility does not affect other permanents")
    void doesNotAffectOtherPermanents() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        addAttachedAura(player1, creature1);

        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttachedAura(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        Permanent aura = new Permanent(new Indestructibility());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
