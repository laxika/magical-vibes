package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillageWatch.class, VillageReavers.class, WyluliWolf.class, GrizzlyBears.class})
class VillageWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Night transforms Village Watch and grants haste to its controller's Wolves and Werewolves")
    void nightFaceGrantsHasteToOwnWolvesAndWerewolves() {
        gd.dayNight = DayNight.NIGHT;
        Permanent watch = harness.addToBattlefieldAndReturn(player1, new VillageWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new WyluliWolf());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentWolf = harness.addToBattlefieldAndReturn(player2, new WyluliWolf());

        assertThat(watch.isTransformed()).isTrue();
        assertThat(gqs.hasKeyword(gd, watch, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentWolf, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Day and night transform Village Watch in both directions")
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent watch = harness.addToBattlefieldAndReturn(player1, new VillageWatch());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(watch.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUntap(player2);
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(watch.isTransformed()).isFalse();
    }

    private void advanceToUntap(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
