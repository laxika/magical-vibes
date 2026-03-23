package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DubTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Dub has correct card properties")
    void hasCorrectProperties() {
        Dub card = new Dub();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(StaticBoostEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantKeywordEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(2)).isInstanceOf(GrantSubtypeEffect.class);

        GrantKeywordEffect keywordEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(keywordEffect.keywords()).containsExactly(Keyword.FIRST_STRIKE);
        assertThat(keywordEffect.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);

        GrantSubtypeEffect subtypeEffect = (GrantSubtypeEffect) card.getEffects(EffectSlot.STATIC).get(2);
        assertThat(subtypeEffect.subtype()).isEqualTo(CardSubtype.KNIGHT);
        assertThat(subtypeEffect.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);
        assertThat(subtypeEffect.overriding()).isFalse();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Dub puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dub");
    }

    @Test
    @DisplayName("Resolving Dub attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dub")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== +2/+2 boost =====

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent dubPerm = new Permanent(new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dubPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    // ===== First strike =====

    @Test
    @DisplayName("Enchanted creature has first strike")
    void enchantedCreatureHasFirstStrike() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent dubPerm = new Permanent(new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dubPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Knight subtype =====

    @Test
    @DisplayName("Enchanted creature gains Knight subtype")
    void enchantedCreatureGainsKnightSubtype() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent dubPerm = new Permanent(new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dubPerm);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(bonus.subtypeOverriding()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature retains its original subtypes")
    void enchantedCreatureRetainsOriginalSubtypes() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent dubPerm = new Permanent(new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dubPerm);

        // Grizzly Bears is a Bear — it should keep that subtype
        assertThat(bearsPerm.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses boost, first strike, and Knight subtype when Dub is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent dubPerm = new Permanent(new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dubPerm);

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
        GameQueryService.StaticBonus bonusBefore = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonusBefore.grantedSubtypes()).contains(CardSubtype.KNIGHT);

        // Remove Dub
        gd.playerBattlefields.get(player1.getId()).remove(dubPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isFalse();
        GameQueryService.StaticBonus bonusAfter = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonusAfter.grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Dub")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dub")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Dub()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Dub does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent dubPerm = new Permanent(new Dub());
        dubPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dubPerm);

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, otherBears);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);
    }
}
