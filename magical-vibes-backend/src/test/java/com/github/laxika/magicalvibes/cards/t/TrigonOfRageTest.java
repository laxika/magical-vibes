package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrigonOfRageTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        TrigonOfRage card = new TrigonOfRage();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has two activated abilities")
    void hasTwoActivatedAbilities() {
        TrigonOfRage card = new TrigonOfRage();

        assertThat(card.getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("First ability: {R}{R}, tap to put a charge counter")
    void hasChargeCounterAbility() {
        TrigonOfRage card = new TrigonOfRage();

        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost().toString()).isEqualTo("{R}{R}");
        assertThat(ability.getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof PutChargeCounterOnSelfEffect);
    }

    @Test
    @DisplayName("Second ability: {2}, tap, remove charge counter to boost target creature +3/+0")
    void hasBoostAbility() {
        TrigonOfRage card = new TrigonOfRage();

        var ability = card.getActivatedAbilities().get(1);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost().toString()).isEqualTo("{2}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof BoostTargetCreatureEffect bte && bte.powerBoost() == 3 && bte.toughnessBoost() == 0);
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfRage()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        assertThat(trigon.getChargeCounters()).isEqualTo(3);
    }

    // ===== Ability 1: Put a charge counter =====

    @Test
    @DisplayName("Activating first ability adds a charge counter")
    void activateFirstAbilityAddsCounter() {
        harness.addToBattlefield(player1, new TrigonOfRage());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        harness.addMana(player1, ManaColor.RED, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("First ability requires red mana")
    void firstAbilityRequiresRedMana() {
        harness.addToBattlefield(player1, new TrigonOfRage());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        // Only colorless mana, should fail
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 2: Boost target creature =====

    @Test
    @DisplayName("Activating second ability gives target creature +3/+0 until end of turn")
    void activateSecondAbilityBoostsCreature() {
        harness.addToBattlefield(player1, new TrigonOfRage());
        harness.addToBattlefield(player2, new GoblinPiker());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(trigon.getChargeCounters()).isEqualTo(2);

        // Target creature gets +3/+0
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("+3/+0 boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new TrigonOfRage());
        harness.addToBattlefield(player2, new GoblinPiker());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        // Goblin Piker is 2/1, +3/+0 = 5/1
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Back to 2/1
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate second ability with 0 charge counters")
    void cannotActivateBoostAbilityWithNoCounters() {
        harness.addToBattlefield(player1, new TrigonOfRage());
        harness.addToBattlefield(player2, new GoblinPiker());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(0);

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can boost multiple times by untapping between uses")
    void canBoostMultipleTimes() {
        harness.addToBattlefield(player1, new TrigonOfRage());
        harness.addToBattlefield(player2, new GoblinPiker());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        // First activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();
        trigon.untap();

        // Second activation
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(trigon.getChargeCounters()).isEqualTo(1);
        // Goblin Piker is 2/1, +3/+0 twice = 8/1
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot activate second ability while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new TrigonOfRage());
        harness.addToBattlefield(player2, new GoblinPiker());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        // First activation taps it
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new TrigonOfRage());
        harness.addToBattlefield(player2, new GoblinPiker());

        Permanent trigon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trigon of Rage"))
                .findFirst().orElseThrow();
        trigon.setChargeCounters(3);

        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
