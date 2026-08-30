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

@CardUsed({CandlegroveWitch.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class})
class CandlegroveWitchTest extends BaseCardTest {

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
    @DisplayName("Coven grants flying when you control three creatures with different powers")
    void grantsFlyingWithCoven() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flying when your creatures do not have three different powers")
    void doesNotGrantFlyingWithoutCoven() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, witch, Keyword.FLYING)).isFalse();
    }
}
