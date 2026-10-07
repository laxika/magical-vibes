package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SurrakarMarauder.class, Forest.class})
class SurrakarMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Surrakar Marauder intimidate until end of turn")
    void landfallGrantsIntimidate() {
        Permanent marauder = harness.addToBattlefieldAndReturn(player1, new SurrakarMarauder());
        harness.setHand(player1, List.of(new Forest()));

        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isFalse();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Landfall intimidate wears off at end of turn")
    void landfallIntimidateWearsOff() {
        Permanent marauder = harness.addToBattlefieldAndReturn(player1, new SurrakarMarauder());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's landfall does not grant intimidate")
    void opponentLandDoesNotTrigger() {
        Permanent marauder = harness.addToBattlefieldAndReturn(player1, new SurrakarMarauder());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Landfall waits for resolution and grants intimidate only to its source")
    void landfallWaitsForResolutionAndAffectsOnlySource() {
        Permanent marauder = harness.addToBattlefieldAndReturn(player1, new SurrakarMarauder());
        Permanent opponentMarauder = harness.addToBattlefieldAndReturn(player2, new SurrakarMarauder());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isFalse();

        Permanent lateMarauder = harness.addToBattlefieldAndReturn(player1, new SurrakarMarauder());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isTrue();
        assertThat(gqs.hasKeyword(gd, lateMarauder, Keyword.INTIMIDATE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentMarauder, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Intimidate remains during the end step before cleanup")
    void intimidateRemainsDuringEndStep() {
        Permanent marauder = harness.addToBattlefieldAndReturn(player1, new SurrakarMarauder());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gqs.hasKeyword(gd, marauder, Keyword.INTIMIDATE)).isTrue();
    }
}
