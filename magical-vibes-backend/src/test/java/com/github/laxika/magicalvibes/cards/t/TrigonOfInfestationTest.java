package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrigonOfInfestationTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        TrigonOfInfestation card = new TrigonOfInfestation();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        TrigonOfInfestation card = new TrigonOfInfestation();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability: {G}{G}, tap to put a charge counter")
    void hasChargeCounterAbility() {
        TrigonOfInfestation card = new TrigonOfInfestation();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost().toString()).isEqualTo("{G}{G}");
        assertThat(ability.getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof PutChargeCounterOnSelfEffect);
    }

    @Test
    @DisplayName("Second ability: {2}, tap, remove charge counter to create 1/1 infect token")
    void hasTokenCreationAbility() {
        TrigonOfInfestation card = new TrigonOfInfestation();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost().toString()).isEqualTo("{2}");
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof CreateCreatureTokenEffect);
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfInfestation()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        assertThat(trigon.getChargeCounters()).isEqualTo(3);
    }

    // ===== Ability 1: Put a charge counter =====

    @Test
    @DisplayName("Activating first ability adds a charge counter")
    void activateFirstAbilityAddsCounter() {
        harness.addToBattlefield(player1, new TrigonOfInfestation());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        harness.addMana(player1, ManaColor.GREEN, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("First ability requires green mana")
    void firstAbilityRequiresGreenMana() {
        harness.addToBattlefield(player1, new TrigonOfInfestation());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        // Only colorless mana, should fail
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 2: Create token =====

    @Test
    @DisplayName("Activating second ability creates a 1/1 green Phyrexian Insect token with infect")
    void activateSecondAbilityCreatesToken() {
        harness.addToBattlefield(player1, new TrigonOfInfestation());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(trigon.getChargeCounters()).isEqualTo(2);

        // 1/1 Phyrexian Insect token on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Insect")
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getKeywords().contains(Keyword.INFECT));
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateTokenAbilityWithNoCounters() {
        harness.addToBattlefield(player1, new TrigonOfInfestation());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can create multiple tokens by activating multiple times (untapping between uses)")
    void canCreateMultipleTokens() {
        harness.addToBattlefield(player1, new TrigonOfInfestation());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(1);

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Insect"))
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new TrigonOfInfestation());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Infestation"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, null);
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
