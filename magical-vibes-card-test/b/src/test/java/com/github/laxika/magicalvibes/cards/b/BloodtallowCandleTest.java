package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodtallowCandle.class, BalothGorger.class, PrimordialWurm.class})
class BloodtallowCandleTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives target creature -5/-5 and sacrifices Bloodtallow Candle")
    void abilityGivesMinusFiveMinusFive() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Bloodtallow Candle should be sacrificed
        harness.assertNotOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertInGraveyard(player1, "Bloodtallow Candle");

        // Target creature should have -5/-5
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability kills a creature with 5 or less toughness")
    void abilityKillsSmallCreature() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Baloth Gorger"));
        harness.passBothPriorities();

        // Baloth Gorger (4/4) should die from -5/-5
        harness.assertNotOnBattlefield(player2, "Baloth Gorger");
        harness.assertInGraveyard(player2, "Baloth Gorger");
    }

    @Test
    @DisplayName("Ability goes on the stack before resolution")
    void abilityUsesStack() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Baloth Gorger"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bloodtallow Candle");
    }

    @Test
    @DisplayName("Bloodtallow Candle is sacrificed immediately as a cost, before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Baloth Gorger"));

        // Before resolution, Bloodtallow Candle should already be sacrificed
        harness.assertNotOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertInGraveyard(player1, "Bloodtallow Candle");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 5); // only 5, need 6

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Baloth Gorger")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Tap the candle manually
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Baloth Gorger")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("-5/-5 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);

        // Advance to cleanup
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Baloth Gorger"));

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Ability should fizzle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));

        // Bloodtallow Candle is still sacrificed (cost already paid)
        harness.assertNotOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertInGraveyard(player1, "Bloodtallow Candle");
    }

    @Test
    @DisplayName("Ability can target a creature you control")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new BloodtallowCandle());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Bloodtallow Candle");
        harness.assertOnBattlefield(player1, "Primordial Wurm");
    }

    @Test
    @DisplayName("Cannot activate targeting a noncreature artifact")
    void cannotTargetNoncreature() {
        Permanent candle = harness.addToBattlefieldAndReturn(player1, new BloodtallowCandle());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, candle.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(candle.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertNotInGraveyard(player1, "Bloodtallow Candle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a target creature")
    void cannotActivateWithoutTarget() {
        Permanent candle = harness.addToBattlefieldAndReturn(player1, new BloodtallowCandle());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(candle.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Bloodtallow Candle");
        harness.assertNotInGraveyard(player1, "Bloodtallow Candle");
        assertThat(gd.stack).isEmpty();
    }
}
