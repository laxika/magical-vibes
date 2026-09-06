package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed(CabarettiInitiate.class)
class CabarettiInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Gains double strike when activated with red mana")
    void gainsDoubleStrikeWithRedMana() {
        Permanent initiate = addInitiateReady(player1);
        addActivationMana(player1, ManaColor.RED);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Can pay the hybrid mana with white mana")
    void gainsDoubleStrikeWithWhiteMana() {
        Permanent initiate = addInitiateReady(player1);
        addActivationMana(player1, ManaColor.WHITE);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent initiate = addInitiateReady(player1);
        addActivationMana(player1, ManaColor.RED);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, initiate, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addInitiateReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addInitiateReady(Player player) {
        return addCreatureReady(player, new CabarettiInitiate());
    }

    private void addActivationMana(Player player, ManaColor hybridColor) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, hybridColor, 1);
    }
}
