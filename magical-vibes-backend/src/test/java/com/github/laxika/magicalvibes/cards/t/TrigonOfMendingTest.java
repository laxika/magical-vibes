package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrigonOfMendingTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        TrigonOfMending card = new TrigonOfMending();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        TrigonOfMending card = new TrigonOfMending();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability: {W}{W}, {T} to put a charge counter on self")
    void firstAbilityStructure() {
        TrigonOfMending card = new TrigonOfMending();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{W}{W}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(PutChargeCounterOnSelfEffect.class);
    }

    @Test
    @DisplayName("Second ability: {2}, {T}, remove a charge counter to target player gains 3 life")
    void secondAbilityStructure() {
        TrigonOfMending card = new TrigonOfMending();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof TargetPlayerGainsLifeEffect tp && tp.amount() == 3);
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfMending()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        assertThat(trigon.getChargeCounters()).isEqualTo(3);
    }

    // ===== First activated ability: put a charge counter =====

    @Test
    @DisplayName("First ability adds a charge counter")
    void firstAbilityAddsChargeCounter() {
        harness.addToBattlefield(player1, new TrigonOfMending());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(1);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(2);
    }

    // ===== Second activated ability: target player gains 3 life =====

    @Test
    @DisplayName("Second ability removes a charge counter and target player gains 3 life")
    void secondAbilityGainsLife() {
        harness.addToBattlefield(player1, new TrigonOfMending());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        int initialLife = gd.playerLifeTotals.get(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife + 3);
    }

    @Test
    @DisplayName("Can target opponent to gain life")
    void canTargetOpponentToGainLife() {
        harness.addToBattlefield(player1, new TrigonOfMending());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(1);

        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife + 3);
    }

    @Test
    @DisplayName("Can activate second ability multiple times with enough counters (untapping between)")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new TrigonOfMending());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        int initialLife = gd.playerLifeTotals.get(player1.getId());

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife + 6);
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateWithNoCounters() {
        harness.addToBattlefield(player1, new TrigonOfMending());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new TrigonOfMending());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Mending"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
