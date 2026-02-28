package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SnapsailGliderTest extends BaseCardTest {

    // ===== Without metalcraft =====

    @Test
    @DisplayName("No flying with only itself on the battlefield (one artifact)")
    void noFlyingWithOnlyItself() {
        harness.addToBattlefield(player1, new SnapsailGlider());

        Permanent glider = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No flying with only two artifacts (itself + one other)")
    void noFlyingWithTwoArtifacts() {
        harness.addToBattlefield(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent glider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapsail Glider"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Has flying with three artifacts (itself + two others)")
    void hasFlyingWithThreeArtifacts() {
        harness.addToBattlefield(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent glider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapsail Glider"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Loses flying when artifact count drops below three")
    void losesFlyingWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new SnapsailGlider());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent glider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Snapsail Glider"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new SnapsailGlider());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new LeoninScimitar());

        Permanent glider = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }
}
