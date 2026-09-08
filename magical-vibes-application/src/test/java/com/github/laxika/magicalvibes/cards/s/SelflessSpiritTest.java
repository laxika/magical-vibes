package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelflessSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Selfless Spirit grants indestructible to your creatures")
    void sacrificeGrantsIndestructibleToOwnCreatures() {
        addReadySelflessSpirit(player1);
        Permanent bears = addReadyCreature(player1);
        Permanent opponentBears = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertNotOnBattlefield(player1, "Selfless Spirit");
        harness.assertInGraveyard(player1, "Selfless Spirit");
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleResetsAtEndOfTurn() {
        addReadySelflessSpirit(player1);
        Permanent bears = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReadySelflessSpirit(Player player) {
        return addCreatureReady(player, new SelflessSpirit());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
