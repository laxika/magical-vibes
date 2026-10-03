package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowingElk.class, GrizzlyBears.class, SnowCoveredForest.class})
class BellowingElkTest extends BaseCardTest {

    @Test
    void creatureEnteringBeforeElkCounts() {
        harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());

        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void creatureThatLeftBattlefieldStillCounts() {
        Permanent other = harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, other));
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void noncreatureEnteringDoesNotCount() {
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        harness.enterBattlefieldAndReturn(player1, new SnowCoveredForest());

        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void keywordsDisappearOnNextTurn() {
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not have trample or indestructible without another creature entering")
    void noKeywordsWithoutAnotherCreatureEntering() {
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());

        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample and indestructible after another creature enters under its control")
    void gainsKeywordsAfterAnotherCreatureEnters() {
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature entering does not grant the keywords")
    void opponentCreatureEnteringDoesNotCount() {
        Permanent elk = harness.enterBattlefieldAndReturn(player1, new BellowingElk());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, elk, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elk, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
