package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CandlelitCavalry.class, GrizzlyBears.class, HillGiant.class})
class CandlelitCavalryTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void endTurn() {
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Coven grants trample when you control three creatures with different powers")
    void grantsTrampleWithCoven() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample when your creatures do not have three different powers")
    void doesNotGrantTrampleWithoutCoven() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new CandlelitCavalry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.TRAMPLE)).isFalse();
    }
}
