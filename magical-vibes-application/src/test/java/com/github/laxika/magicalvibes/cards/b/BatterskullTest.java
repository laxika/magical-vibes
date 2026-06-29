package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatterskullTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Batterskull has living weapon ETB effect")
    void hasLivingWeaponEffect() {
        Batterskull card = new Batterskull();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(LivingWeaponEffect.class);
    }

    @Test
    @DisplayName("Batterskull has static +4/+4 boost, vigilance, and lifelink")
    void hasStaticBoostAndKeywords() {
        Batterskull card = new Batterskull();

        StaticBoostEffect boost = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof StaticBoostEffect)
                .map(e -> (StaticBoostEffect) e)
                .findFirst().orElseThrow();
        assertThat(boost.powerBoost()).isEqualTo(4);
        assertThat(boost.toughnessBoost()).isEqualTo(4);

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .filter(e -> e.scope() == GrantScope.EQUIPPED_CREATURE)
                .toList();
        assertThat(keywordEffects).hasSize(2);
        assertThat(keywordEffects).flatExtracting(GrantKeywordEffect::keywords)
                .containsExactlyInAnyOrder(Keyword.VIGILANCE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Batterskull has {3} return to hand ability and equip {5} ability")
    void hasActivatedAbilities() {
        Batterskull card = new Batterskull();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {3}: Return to hand
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(ReturnSelfToHandEffect.class);

        // Ability 1: Equip {5}
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{5}");
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(1).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Living weapon ETB =====

    @Test
    @DisplayName("Casting Batterskull triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Batterskull");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        Permanent batterskull = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Batterskull"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(batterskull.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        assertThat(germ.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(germ.getCard().getPower()).isEqualTo(0);
        assertThat(germ.getCard().getToughness()).isEqualTo(0);
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(germ.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GERM);
    }

    // ===== Germ gets equipment bonuses =====

    @Test
    @DisplayName("Germ token gets +4/+4, vigilance, and lifelink from Batterskull")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // 0/0 base + 4/4 from equipment = 4/4 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, germ, Keyword.LIFELINK)).isTrue();
    }

    // ===== Equip to another creature =====

    @Test
    @DisplayName("Equipping Batterskull to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Permanent batterskull = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Batterskull"))
                .findFirst().orElseThrow();

        assertThat(batterskull.getAttachedTo()).isEqualTo(bears.getId());

        // Bears should get +4/+4, vigilance, and lifelink
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);  // 2 + 4
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);  // 2 + 4
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    // ===== Germ dies when equipment is moved =====

    @Test
    @DisplayName("Germ token dies (0 toughness) when Batterskull is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));
    }

    // ===== Return to hand ability =====

    @Test
    @DisplayName("Activating {3} ability returns Batterskull to hand")
    void returnToHandAbility() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Batterskull and Germ are on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Batterskull"));

        // Activate {3}: Return to hand
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Batterskull should be in hand
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Batterskull"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Batterskull"));
    }

    @Test
    @DisplayName("Germ dies when Batterskull is returned to hand")
    void germDiesWhenBatterskullReturnedToHand() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Activate {3}: Return to hand
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Germ should be dead (0/0 without equipment)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Germ"));
    }

    // ===== Equipment stays when Germ is removed =====

    @Test
    @DisplayName("Batterskull stays on battlefield when Germ is removed")
    void equipmentStaysWhenGermIsRemoved() {
        harness.setHand(player1, List.of(new Batterskull()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(germ);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Batterskull"));
    }
}
