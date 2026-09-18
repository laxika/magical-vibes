package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagefireHellkite.class, GrizzlyBears.class})
class RagefireHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Ragefire Hellkite double strike")
    void sacrificingAnotherCreatureGrantsDoubleStrike() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not give Ragefire Hellkite double strike")
    void decliningSacrificeDoesNothing() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void attackAndAcceptMay() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
