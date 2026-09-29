package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosTerminatorLord.class, GrizzlyBears.class})
class ChaosTerminatorLordTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat on your turn, another creature you control gains double strike")
    void grantsDoubleStrikeAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new ChaosTerminatorLord());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself or an opponent's creature")
    void cannotTargetSelfOrOpponentCreature() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new ChaosTerminatorLord());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lord.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
