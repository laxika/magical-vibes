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

@CardUsed(LionheartMaverick.class)
class LionheartMaverickTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives +1/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent maverick = addReadyMaverick(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isEqualTo(1);
        assertThat(maverick.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple activations give a cumulative boost")
    void repeatedActivationsStack() {
        Permanent maverick = addReadyMaverick(player1);
        addAbilityMana(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isEqualTo(2);
        assertThat(maverick.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent maverick = addReadyMaverick(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maverick.getPowerModifier()).isZero();
        assertThat(maverick.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability requires four generic mana and one white mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyMaverick(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }

    private Permanent addReadyMaverick(Player player) {
        Permanent permanent = new Permanent(new LionheartMaverick());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
