package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillageWatch.class, VillageReavers.class, SnarlingWolf.class, TimberlandGuide.class})
class VillageWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Night transforms Village Watch and grants haste to its controller's Wolves and Werewolves")
    void nightFaceGrantsHasteToOwnWolvesAndWerewolves() {
        gd.dayNight = DayNight.NIGHT;
        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new TimberlandGuide());
        Permanent opponentWolf = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());

        assertThat(watch.isTransformed()).isTrue();
        assertThat(gqs.hasKeyword(gd, watch, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, guide, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentWolf, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Day and night transform Village Watch in both directions")
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(watch.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player2);
        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(watch.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Entering before day or night is established makes it day without granting haste to Wolves")
    void enteringEstablishesDayWithoutGrantingHaste() {
        gd.dayNight = DayNight.NEITHER;
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());

        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(watch.isTransformed()).isFalse();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Becoming day removes the night face's haste grant")
    void becomingDayRemovesHasteGrant() {
        gd.dayNight = DayNight.NIGHT;
        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isTrue();

        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(watch.isTransformed()).isFalse();
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The night face grants haste continuously and only while it remains on the battlefield")
    void hasteGrantUpdatesForEnteringWolvesAndLeavingSource() {
        gd.dayNight = DayNight.NIGHT;
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());
        Permanent laterWolf = harness.enterBattlefieldAndReturn(player1, new SnarlingWolf());

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterWolf, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(watch);

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterWolf, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the previous active player's spells determine whether night becomes day")
    void spellsCastDuringAnotherPlayersTurnDoNotMakeItDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(watch.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Spells cast by the nonactive player do not prevent day from becoming night")
    void nonactivePlayersSpellsDoNotPreventNight() {
        gd.dayNight = DayNight.DAY;
        Permanent watch = harness.enterBattlefieldAndReturn(player1, new VillageWatch());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 0);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(watch.isTransformed()).isTrue();
    }
}
