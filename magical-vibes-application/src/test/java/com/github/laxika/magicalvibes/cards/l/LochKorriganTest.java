package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LochKorrigan.class})
class LochKorriganTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +1/+1, payable with blue mana")
    void resolvingBoostsWithBlue() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(2);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability is also payable with black mana (hybrid cost)")
    void payableWithBlack() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(2);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate multiple times, stacking the boost")
    void stacksMultipleActivations() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(3);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent korrigan = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(korrigan.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(korrigan.getEffectivePower()).isEqualTo(1);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addKorriganReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addKorriganReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent korrigan = harness.addToBattlefieldAndReturn(player1, new LochKorrigan());
        korrigan.setSummoningSick(true);
        korrigan.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(korrigan.getEffectivePower()).isEqualTo(2);
        assertThat(korrigan.getEffectiveToughness()).isEqualTo(2);
        assertThat(korrigan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost applies only to its source and spends the hybrid mana")
    void boostsOnlySourceAndSpendsMana() {
        Permanent source = addKorriganReady(player1);
        Permanent other = addKorriganReady(player1);
        Permanent opposing = addKorriganReady(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.getEffectivePower()).isEqualTo(1);
        assertThat(other.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposing.getEffectivePower()).isEqualTo(1);
        assertThat(opposing.getEffectiveToughness()).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addKorriganReady(Player player) {
        return addCreatureReady(player, new LochKorrigan());
    }
}
