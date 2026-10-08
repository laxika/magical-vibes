package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnapsailGlider.class, AccordersShield.class})
class SnapsailGliderTest extends BaseCardTest {

    @Test
    @DisplayName("No flying with only itself on the battlefield (one artifact)")
    void noFlyingWithOnlyItself() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with only two artifacts (itself + one other)")
    void noFlyingWithTwoArtifacts() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying with three artifacts (itself + two others)")
    void hasFlyingWithThreeArtifacts() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when artifact count drops below three")
    void losesFlyingWhenArtifactRemoved() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying immediately when a third artifact enters")
    void gainsFlyingWhenThirdArtifactEnters() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new AccordersShield());
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Still has flying with more than three artifacts, including tapped artifacts")
    void hasFlyingWithFourArtifactsIncludingTappedArtifacts() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());
        harness.addToBattlefieldAndReturn(player1, new AccordersShield()).setTapped(true);
        harness.addToBattlefieldAndReturn(player1, new AccordersShield()).setTapped(true);
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new SnapsailGlider());
        harness.addToBattlefield(player2, new AccordersShield());
        harness.addToBattlefield(player2, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }
}
