package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RegisaurAlpha;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderingSpinebackTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Thundering Spineback has static boost effect for Dinosaurs")
    void hasCorrectStaticEffect() {
        ThunderingSpineback card = new ThunderingSpineback();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);
    }

    @Test
    @DisplayName("Thundering Spineback has activated ability to create Dinosaur token")
    void hasCorrectActivatedAbility() {
        ThunderingSpineback card = new ThunderingSpineback();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{5}{G}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect tokenEffect = (CreateTokenEffect) ability.getEffects().getFirst();
        assertThat(tokenEffect.tokenName()).isEqualTo("Dinosaur");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(3);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.GREEN);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.TRAMPLE);
    }

    // ===== Static effect: buffs other Dinosaurs you control =====

    @Test
    @DisplayName("Other Dinosaur creatures you control get +1/+1")
    void buffsOtherDinosaursYouControl() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent dino = findPermanent(player1, "Regisaur Alpha");

        // Regisaur Alpha is 4/4 base + 1/1 from Thundering Spineback = 5/5
        assertThat(gqs.getEffectivePower(gd, dino)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dino)).isEqualTo(5);
    }

    @Test
    @DisplayName("Thundering Spineback does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ThunderingSpineback());

        Permanent spineback = findPermanent(player1, "Thundering Spineback");

        assertThat(gqs.getEffectivePower(gd, spineback)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spineback)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff non-Dinosaur creatures")
    void doesNotBuffNonDinosaurs() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Dinosaur creatures")
    void doesNotBuffOpponentDinosaurs() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player2, new RegisaurAlpha());

        Permanent opponentDino = findPermanent(player2, "Regisaur Alpha");

        assertThat(gqs.getEffectivePower(gd, opponentDino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentDino)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Thundering Spineback leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent dino = findPermanent(player1, "Regisaur Alpha");
        assertThat(gqs.getEffectivePower(gd, dino)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Thundering Spineback"));

        assertThat(gqs.getEffectivePower(gd, dino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dino)).isEqualTo(4);
    }

    // ===== Activated ability: create Dinosaur token =====

    @Test
    @DisplayName("Activated ability creates a 3/3 green Dinosaur token with trample")
    void activatedAbilityCreatesToken() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Token should be on battlefield (plus Thundering Spineback itself)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dinosaur"))
                .findFirst().orElseThrow();

        // Token is 3/3 base + 1/1 from Thundering Spineback lord = 4/4
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Activated ability can be used multiple times per turn")
    void activatedAbilityCanBeUsedMultipleTimes() {
        harness.addToBattlefield(player1, new ThunderingSpineback());

        // First activation
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Second activation
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dinosaur"))
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability does not require tap")
    void activatedAbilityDoesNotRequireTap() {
        ThunderingSpineback card = new ThunderingSpineback();
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
    }

    // ===== Helper methods =====

}
