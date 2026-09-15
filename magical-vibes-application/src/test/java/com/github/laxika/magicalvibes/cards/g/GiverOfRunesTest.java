package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GiverOfRunes.class)
class GiverOfRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Giver of Runes grants another creature protection from a chosen color")
    void grantsProtectionFromChosenColor() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(giver.isTapped()).isTrue();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can grant protection from colorless")
    void grantsProtectionFromColorless() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(giver.isTapped()).isTrue();
        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target itself, an opponent's creature, or a noncreature")
    void requiresAnotherCreatureYouControl() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent opponentCreature = addCreatureReady(player2, new GiverOfRunes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }
}
