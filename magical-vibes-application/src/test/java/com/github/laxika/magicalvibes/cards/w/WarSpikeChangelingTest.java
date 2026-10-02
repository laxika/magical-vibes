package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WarSpikeChangeling.class)
class WarSpikeChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving {R} ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent changeling = addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike granted by ability wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent changeling = addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new WarSpikeChangeling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation consumes exactly one red mana")
    void activationConsumesExactlyOneRedMana() {
        addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new WarSpikeChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
