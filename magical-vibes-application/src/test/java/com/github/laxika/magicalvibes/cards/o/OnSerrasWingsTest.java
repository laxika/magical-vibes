package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OnSerrasWingsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("On Serra's Wings has correct card properties")
    void hasCorrectProperties() {
        OnSerrasWings card = new OnSerrasWings();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(StaticBoostEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect keywordEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(keywordEffect.keywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.VIGILANCE, Keyword.LIFELINK);
        assertThat(keywordEffect.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);
        assertThat(card.getEffects(EffectSlot.STATIC).get(2)).isInstanceOf(GrantSupertypeToEnchantedPermanentEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting On Serra's Wings puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("On Serra's Wings");
    }

    @Test
    @DisplayName("Resolving On Serra's Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("On Serra's Wings")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== +1/+1 boost =====

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(3);
    }

    // ===== Keywords =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void enchantedCreatureHasVigilance() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has lifelink")
    void enchantedCreatureHasLifelink() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();
    }

    // ===== Legendary supertype =====

    @Test
    @DisplayName("Enchanted creature gains legendary supertype")
    void enchantedCreatureBecomesLegendary() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Grizzly Bears is not legendary
        assertThat(bearsPerm.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        var bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses all bonuses when On Serra's Wings is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, bearsPerm).grantedSupertypes()).contains(CardSupertype.LEGENDARY);

        // Remove On Serra's Wings
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, bearsPerm).grantedSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with On Serra's Wings")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with On Serra's Wings")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new OnSerrasWings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("On Serra's Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent auraPerm = new Permanent(new OnSerrasWings());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Other creature should not be affected
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.LIFELINK)).isFalse();
    }
}
