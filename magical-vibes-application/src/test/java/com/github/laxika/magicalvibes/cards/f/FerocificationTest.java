package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({Ferocification.class, GrizzlyBears.class})
class FerocificationTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a chosen creature you control by +2/+0")
    void boostsChosenCreature() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gets +2/+0 until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Grants menace and haste to a chosen creature you control")
    void grantsMenaceAndHaste() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gains menace and haste until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Only permits creatures you control as targets")
    void onlyTargetsCreaturesYouControl() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gets +2/+0 until end of turn");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingBear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The temporary mode effect wears off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Ferocification());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Target creature you control gets +2/+0 until end of turn");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
