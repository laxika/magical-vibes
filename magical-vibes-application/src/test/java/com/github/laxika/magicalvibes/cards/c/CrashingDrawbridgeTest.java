package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashingDrawbridge.class, GrizzlyBears.class})
class CrashingDrawbridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability grants haste to creatures you control")
    void grantsHasteToOwnCreatures() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drawbridge), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drawbridge, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drawbridge), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drawbridge, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isFalse();
    }
}
