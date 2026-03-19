package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodtallowCandleTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bloodtallow Candle has correct activated ability")
    void hasCorrectProperties() {
        BloodtallowCandle card = new BloodtallowCandle();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{6}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect effect = (BoostTargetCreatureEffect) ability.getEffects().get(1);
        assertThat(effect.powerBoost()).isEqualTo(-5);
        assertThat(effect.toughnessBoost()).isEqualTo(-5);
    }

    // ===== Ability resolution =====

    @Test
    @DisplayName("Activated ability gives target creature -5/-5 and sacrifices Bloodtallow Candle")
    void abilityGivesMinusFiveMinusFive() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        // Use a big creature so it survives -5/-5
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(7);
        bigCreature.setToughness(7);
        harness.addToBattlefield(player2, bigCreature);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        // Bloodtallow Candle should be sacrificed
        harness.assertNotOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertInGraveyard(player1, "Bloodtallow Candle");

        // Target creature should have -5/-5
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability kills a creature with 5 or less toughness")
    void abilityKillsSmallCreature() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should die from -5/-5
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Stack behavior =====

    @Test
    @DisplayName("Ability goes on the stack before resolution")
    void abilityUsesStack() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bloodtallow Candle");
    }

    @Test
    @DisplayName("Bloodtallow Candle is sacrificed immediately as a cost, before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        // Before resolution, Bloodtallow Candle should already be sacrificed
        harness.assertNotOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertInGraveyard(player1, "Bloodtallow Candle");
    }

    // ===== Cost enforcement =====

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5); // only 5, need 6

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Tap the candle manually
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Debuff wears off =====

    @Test
    @DisplayName("-5/-5 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        // Use a big creature so it survives -5/-5
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(8);
        bigCreature.setToughness(8);
        harness.addToBattlefield(player2, bigCreature);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);

        // Advance to cleanup
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Ability should fizzle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));

        // Bloodtallow Candle is still sacrificed (cost already paid)
        harness.assertNotOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertInGraveyard(player1, "Bloodtallow Candle");
    }
}
