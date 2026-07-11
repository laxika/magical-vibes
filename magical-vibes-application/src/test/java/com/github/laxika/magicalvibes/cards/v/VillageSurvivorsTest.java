package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VillageSurvivorsTest extends BaseCardTest {

    // ===== Above threshold (default 20 life) =====

    @Test
    @DisplayName("No vigilance grant to other creatures at default 20 life")
    void noVigilanceAtDefaultLife() {
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("No vigilance grant at 6 life (just above threshold)")
    void noVigilanceAt6Life() {
        gd.playerLifeTotals.put(player1.getId(), 6);
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    // ===== At or below threshold =====

    @Test
    @DisplayName("Grants vigilance to other creatures at exactly 5 life")
    void grantsVigilanceAtExactly5Life() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Grants vigilance to other creatures below 5 life")
    void grantsVigilanceBelow5Life() {
        gd.playerLifeTotals.put(player1.getId(), 1);
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Does not grant to opponent's creatures =====

    @Test
    @DisplayName("Does not grant vigilance to opponent's creatures")
    void doesNotGrantVigilanceToOpponentCreatures() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Only the controller's life total matters, not the opponent's")
    void opponentLifeDoesNotCount() {
        gd.playerLifeTotals.put(player2.getId(), 1);
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Grant is dynamic =====

    @Test
    @DisplayName("Gains and loses vigilance grant as life crosses the threshold")
    void vigilanceGrantIsDynamic() {
        harness.addToBattlefield(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // 20 life — no grant
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();

        // Drop to 5 — grant applies
        gd.playerLifeTotals.put(player1.getId(), 5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        // Back above threshold — grant gone
        gd.playerLifeTotals.put(player1.getId(), 10);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }
}
