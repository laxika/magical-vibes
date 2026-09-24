package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BrittleEffigy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiverOfRunes.class, GrizzlyBears.class, BrittleEffigy.class})
class GiverOfRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Grants protection from a chosen color to another creature you control")
    void grantsProtectionFromChosenColor() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(giver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can grant protection from colorless")
    void grantsProtectionFromColorless() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, null)).isTrue();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Giver of Runes itself")
    void cannotTargetItself() {
        addCreatureReady(player1, new GiverOfRunes());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player1, "Giver of Runes")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

class Mh1GiverOfRunesTest extends BaseCardTest {

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
