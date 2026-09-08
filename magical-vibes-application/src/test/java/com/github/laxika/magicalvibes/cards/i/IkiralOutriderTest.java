package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IkiralOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Ikiral Outrider's base power, toughness, and vigilance")
    void levelsUpAtThresholds() {
        Permanent outrider = addCreatureReady(player1, new IkiralOutrider());

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.VIGILANCE)).isFalse();

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(outrider.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.VIGILANCE)).isTrue();

        for (int i = 0; i < 3; i++) {
            levelUp(player1);
        }

        assertThat(outrider.getCounterCount(CounterType.LEVEL)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Level up can only be activated at sorcery speed")
    void levelUpRequiresSorcerySpeed() {
        Permanent outrider = addCreatureReady(player1, new IkiralOutrider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> levelUp(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(outrider.getCounterCount(CounterType.LEVEL)).isZero();
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 16);
    }

    private void levelUp(Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
