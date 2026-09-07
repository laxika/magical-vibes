package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
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

@CardUsed({RitualGuardian.class, CrawWurm.class, GrizzlyBears.class})
class RitualGuardianTest extends BaseCardTest {

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
    @DisplayName("Coven grants lifelink when you control three creatures with different powers")
    void grantsLifelinkWithCoven() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not grant lifelink when your creatures do not have three different powers")
    void doesNotGrantLifelinkWithoutCoven() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new RitualGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrawWurm());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.LIFELINK)).isFalse();
    }
}
