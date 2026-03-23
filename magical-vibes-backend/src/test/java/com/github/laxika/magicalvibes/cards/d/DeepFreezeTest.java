package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessStaticEffect;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepFreezeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Deep Freeze has correct effects")
    void hasCorrectEffects() {
        DeepFreeze card = new DeepFreeze();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(5);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(SetBasePowerToughnessStaticEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantKeywordEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(2)).isInstanceOf(LosesAllAbilitiesEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(3)).isInstanceOf(GrantColorEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(4)).isInstanceOf(GrantSubtypeEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Deep Freeze and resolving attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Deep Freeze")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Base P/T override =====

    @Test
    @DisplayName("Enchanted creature has base power and toughness 0/4")
    void setsBasePowerToughness() {
        // Air Elemental is a 4/4 with flying
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        // Attach Deep Freeze directly
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        int effectivePower = gqs.getEffectivePower(gd, airElemental);
        int effectiveToughness = gqs.getEffectiveToughness(gd, airElemental);

        assertThat(effectivePower).isEqualTo(0);
        assertThat(effectiveToughness).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters still apply on top of Deep Freeze base P/T")
    void countersApplyOnTopOfBasePT() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.setPlusOnePlusOneCounters(2);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        // Base 0/4 + 2 counters = 2/6
        int effectivePower = gqs.getEffectivePower(gd, bearsPerm);
        int effectiveToughness = gqs.getEffectiveToughness(gd, bearsPerm);

        assertThat(effectivePower).isEqualTo(2);
        assertThat(effectiveToughness).isEqualTo(6);
    }

    // ===== Defender =====

    @Test
    @DisplayName("Enchanted creature has defender")
    void grantsDefender() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.DEFENDER)).isTrue();
    }

    // ===== Loses all abilities =====

    @Test
    @DisplayName("Enchanted creature loses its original keywords like flying")
    void losesOriginalKeywords() {
        // Air Elemental has flying
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        // Air Elemental should have lost flying
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
        // But should still have defender (granted by Deep Freeze)
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature with activated ability cannot use it")
    void losesActivatedAbilities() {
        // Prodigal Pyromancer has a tap ability
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(pyromancer.getId());
        gd.playerBattlefields.get(player2.getId()).add(deepFreezePerm);

        // Attempting to activate the ability should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Color and type changes =====

    @Test
    @DisplayName("Enchanted creature is blue in addition to its other colors")
    void grantsBlueColor() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedColors()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Enchanted creature is a Wall in addition to its other types")
    void grantsWallSubtype() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.WALL);
    }

    // ===== Removal restores everything =====

    @Test
    @DisplayName("Removing Deep Freeze restores creature's original P/T and abilities")
    void removalRestoresOriginalState() {
        // Air Elemental is a 4/4 with flying
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        // Attach Deep Freeze
        Permanent deepFreezePerm = new Permanent(new DeepFreeze());
        deepFreezePerm.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(deepFreezePerm);

        // Verify Deep Freeze effects are active
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isTrue();

        // Remove Deep Freeze
        gd.playerBattlefields.get(player1.getId()).remove(deepFreezePerm);

        // Verify creature is back to normal
        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Deep Freeze")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Add a noncreature permanent
        com.github.laxika.magicalvibes.cards.f.FountainOfYouth artifact = new com.github.laxika.magicalvibes.cards.f.FountainOfYouth();
        harness.addToBattlefield(player1, artifact);
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent artifactPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
