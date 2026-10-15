package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GloomStalker.class)
class GloomStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike before its controller completes a dungeon")
    void noDoubleStrikeBeforeDungeonCompletion() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GloomStalker());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike after its controller completes a dungeon")
    void hasDoubleStrikeAfterDungeonCompletion() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GloomStalker());
        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not get double strike when only an opponent completed a dungeon")
    void opponentDungeonCompletionDoesNotGrantDoubleStrike() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GloomStalker());
        gd.playersWhoCompletedDungeon.add(player2.getId());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike immediately when its controller completes a dungeon")
    void gainsDoubleStrikeWhenDungeonIsCompleted() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GloomStalker());
        Permanent opponentStalker = harness.addToBattlefieldAndReturn(player2, new GloomStalker());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isFalse();
        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentStalker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike when entering after its controller completed a dungeon")
    void hasDoubleStrikeWhenEnteringAfterDungeonCompletion() {
        gd.playersWhoCompletedDungeon.add(player1.getId());
        Permanent stalker = harness.enterBattlefieldAndReturn(player1, new GloomStalker());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike follows the current controller's dungeon completion")
    void doubleStrikeFollowsCurrentController() {
        gd.playersWhoCompletedDungeon.add(player1.getId());
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new GloomStalker());
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(stalker);
        gd.playerBattlefields.get(player2.getId()).add(stalker);

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isFalse();

        gd.playersWhoCompletedDungeon.add(player2.getId());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
