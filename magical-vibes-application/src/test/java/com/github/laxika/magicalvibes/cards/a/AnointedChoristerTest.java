package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnointedChorister.class})
class AnointedChoristerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives +3/+3")
    void abilityBoostsSelf() {
        Permanent chorister = addReadyChorister(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chorister)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chorister)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent chorister = addReadyChorister(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chorister)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chorister)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyChorister(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent chorister = addReadyChorister(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, java.util.List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(chorister.getEffectivePower()).isEqualTo(1);
    }

    private Permanent addReadyChorister(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new AnointedChorister());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Multiple activations stack and both expire at end of turn")
    void multipleActivationsStack() {
        Permanent chorister = addReadyChorister(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chorister)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, chorister)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chorister)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chorister)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability requires white mana even with enough total mana")
    void cannotActivateWithoutWhiteMana() {
        addReadyChorister(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A tapped summoning-sick Chorister can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent chorister = harness.addToBattlefieldAndReturn(player1, new AnointedChorister());
        chorister.setSummoningSick(true);
        chorister.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chorister)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chorister)).isEqualTo(4);
        assertThat(chorister.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boosted combat damage gains the full amount of life")
    void boostedCombatDamageGainsFourLife() {
        addReadyChorister(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, java.util.List.of(0));

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
