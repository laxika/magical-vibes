package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForebearsBladeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Forebear's Blade has static +3/+0 boost effect for equipped creature")
    void hasStaticBoostEffect() {
        ForebearsBlade card = new ForebearsBlade();

        List<StaticBoostEffect> boosts = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .toList();
        assertThat(boosts).hasSize(1);
        assertThat(boosts.getFirst().powerBoost()).isEqualTo(3);
        assertThat(boosts.getFirst().toughnessBoost()).isEqualTo(0);
        assertThat(boosts.getFirst().scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Forebear's Blade grants vigilance and trample to equipped creature")
    void hasKeywordGrantEffects() {
        ForebearsBlade card = new ForebearsBlade();

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect gke && gke.scope() == GrantScope.EQUIPPED_CREATURE)
                .map(e -> (GrantKeywordEffect) e)
                .toList();
        assertThat(keywordEffects).hasSize(2);
        assertThat(keywordEffects).extracting(GrantKeywordEffect::keyword)
                .containsExactlyInAnyOrder(Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Forebear's Blade has equipped creature death trigger with attach effect")
    void hasEquippedCreatureDeathTrigger() {
        ForebearsBlade card = new ForebearsBlade();

        assertThat(card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_EQUIPPED_CREATURE_DIES).getFirst())
                .isInstanceOf(AttachSourceEquipmentToTargetCreatureEffect.class);
    }

    @Test
    @DisplayName("Forebear's Blade has equip {3} ability")
    void hasEquipAbility() {
        ForebearsBlade card = new ForebearsBlade();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Static effects =====

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not get boost or keywords")
    void unequippedCreatureNoBoost() {
        Permanent creature = addReadyCreature(player1);
        addBladeReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses boost and keywords when blade is removed")
    void creatureLosesEffectsWhenBladeRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blade);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When equipped creature dies, blade attaches to target creature you control")
    void deathTriggerAttachesToAnotherCreature() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature1.getId());

        // Opponent destroys the equipped creature with Deathmark
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature1.getId());
        harness.passBothPriorities(); // resolve Deathmark — creature dies, death trigger queued

        // Player chooses creature2 as the target for the blade's death trigger
        harness.handlePermanentChosen(player1, creature2.getId());
        harness.passBothPriorities(); // resolve the attach trigger

        // Blade should now be attached to creature2
        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
        // creature2 should now benefit from the blade's boost
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Death trigger has no valid targets when no other creatures exist — blade stays unattached")
    void deathTriggerNoValidTargets() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // Opponent destroys the only creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Deathmark — creature dies, no valid target

        // Blade should still be on the battlefield but unattached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forebear's Blade"));
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Death trigger does not fire for a different creature dying")
    void triggerDoesNotFireForDifferentCreature() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature1.getId());

        // Opponent destroys the NON-equipped creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature2.getId());
        harness.passBothPriorities(); // resolve Deathmark

        // Blade should still be attached to creature1
        assertThat(blade.getAttachedTo()).isEqualTo(creature1.getId());
    }

    @Test
    @DisplayName("Blade stays on battlefield after equipped creature dies")
    void bladeStaysOnBattlefieldAfterCreatureDies() {
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature1.getId());
        harness.passBothPriorities(); // resolve Deathmark

        // Choose creature2 as target
        harness.handlePermanentChosen(player1, creature2.getId());
        harness.passBothPriorities(); // resolve attach trigger

        // Creature1 should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Blade should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forebear's Blade"));
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches blade to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Blade can be moved to another creature via equip")
    void canReEquipToAnotherCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        blade.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(5);

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(5);
    }

    // ===== Helpers =====

    private Permanent addBladeReady(Player player) {
        Permanent perm = new Permanent(new ForebearsBlade());
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
