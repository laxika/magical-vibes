package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillageSurvivors.class, GrizzlyBears.class})
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

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    @DisplayName("Other creatures attack untapped only while fateful hour applies")
    void vigilanceChangesAttackTappingAtThreshold(int life) {
        harness.setLife(player1, life);
        Permanent survivors = addCreatureReady(player1, new VillageSurvivors());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(survivors.isAttacking()).isTrue();
        assertThat(bears.isAttacking()).isTrue();
        assertThat(survivors.isTapped()).isFalse();
        assertThat(bears.isTapped()).isEqualTo(life > 5);
    }

    @Test
    @DisplayName("Vigilance grant ends when Village Survivors leaves the battlefield")
    void vigilanceGrantEndsWhenSourceLeaves() {
        harness.setLife(player1, 5);
        Permanent survivors = harness.addToBattlefieldAndReturn(player1, new VillageSurvivors());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, survivors);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        harness.assertInGraveyard(player1, "Village Survivors");
    }
}
