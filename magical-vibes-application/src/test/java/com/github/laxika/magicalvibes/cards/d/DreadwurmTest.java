package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadwurm.class, Forest.class})
class DreadwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible when a land you control enters")
    void gainsIndestructibleOnLandfall() {
        Permanent dreadwurm = addDreadwurmReady();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void doesNotTriggerForOpponentsLand() {
        Permanent dreadwurm = harness.addToBattlefieldAndReturn(player1, new Dreadwurm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Loses landfall-granted indestructible at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent dreadwurm = addDreadwurmReady();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadwurm, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addDreadwurmReady() {
        Permanent dreadwurm = harness.addToBattlefieldAndReturn(player1, new Dreadwurm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return dreadwurm;
    }
}
