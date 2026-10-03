package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaravanEscort.class})
class CaravanEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Caravan Escort's base power and toughness at each threshold")
    void levelsUpAtThresholds() {
        Permanent escort = addEscortReady(player1);

        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        levelUp(player1);
        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(2);

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        addEscortReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    @DisplayName("Caravan Escort does not have first strike below level five")
    void doesNotHaveFirstStrikeBelowLevelFive(int level) {
        Permanent escort = addEscortReady(player1);
        escort.setCounterCount(CounterType.LEVEL, level);

        assertThat(gqs.hasKeyword(gd, escort, Keyword.FIRST_STRIKE)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    @DisplayName("Caravan Escort has first strike and is 5/5 at level five and above")
    void hasFirstStrikeAtLevelFiveAndAbove(int level) {
        Permanent escort = addEscortReady(player1);
        escort.setCounterCount(CounterType.LEVEL, level);

        assertThat(gqs.hasKeyword(gd, escort, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(5);
    }

    @Test
    @DisplayName("Losing level counters removes first strike and restores the lower-level size")
    void losingLevelCountersRestoresLowerLevelCharacteristics() {
        Permanent escort = addEscortReady(player1);
        escort.setCounterCount(CounterType.LEVEL, 5);
        assertThat(gqs.hasKeyword(gd, escort, Keyword.FIRST_STRIKE)).isTrue();

        escort.setCounterCount(CounterType.LEVEL, 4);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, escort, Keyword.FIRST_STRIKE)).isFalse();

        escort.setCounterCount(CounterType.LEVEL, 0);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, escort)).isEqualTo(1);
    }

    @Test
    @DisplayName("Level up uses the stack and can be activated while tapped and summoning sick")
    void levelUpUsesStackWithoutTapOrHasteRequirement() {
        Permanent escort = addEscortReady(player1);
        escort.setSummoningSick(true);
        escort.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(escort.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, escort)).isEqualTo(2);
        assertThat(escort.isTapped()).isTrue();
    }

    private Permanent addEscortReady(Player player) {
        return addCreatureReady(player, new CaravanEscort());
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
