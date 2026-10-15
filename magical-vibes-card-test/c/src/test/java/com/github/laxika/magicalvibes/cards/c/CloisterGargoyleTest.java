package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloisterGargoyle.class})
class CloisterGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller venture into a dungeon")
    void entersDungeon() {
        harness.setLibrary(player1, List.of(new CloisterGargoyle()));
        harness.castFromHand(player1, new CloisterGargoyle(), "{2}{W}");

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.playersWhoCompletedDungeon).doesNotContain(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Gets +3/+0 and flying after its controller completes a dungeon")
    void getsBonusAfterDungeonCompletion() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new CloisterGargoyle());
        int powerBefore = gqs.getEffectivePower(gd, gargoyle);
        int toughnessBefore = gqs.getEffectiveToughness(gd, gargoyle);

        gd.playersWhoCompletedDungeon.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's completed dungeon grants neither power nor flying")
    void opponentCompletionDoesNotGrantBonus() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new CloisterGargoyle());
        int powerBefore = gqs.getEffectivePower(gd, gargoyle);
        int toughnessBefore = gqs.getEffectiveToughness(gd, gargoyle);

        gd.playersWhoCompletedDungeon.add(player2.getId());

        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Entering advances an existing dungeon along the controller's chosen path")
    void advancesExistingDungeon() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        harness.castFromHand(player1, new CloisterGargoyle(), "{2}{W}");

        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playersWhoCompletedDungeon).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The bonus begins after the final room ability resolves, not upon entering it")
    void gainsBonusWhenFinalRoomResolves() {
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));
        harness.setLibrary(player1, List.of(new CloisterGargoyle()));
        harness.castFromHand(player1, new CloisterGargoyle(), "{2}{W}");
        harness.passBothPriorities();
        Permanent gargoyle = findPermanent(player1, "Cloister Gargoyle");
        int powerBefore = gqs.getEffectivePower(gd, gargoyle);
        int toughnessBefore = gqs.getEffectiveToughness(gd, gargoyle);

        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 6));
        assertThat(gd.playersWhoCompletedDungeon).doesNotContain(player1.getId());
        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(powerBefore);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();

        resolveAllTriggers();

        assertThat(gd.playersWhoCompletedDungeon).contains(player1.getId());
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gqs.getEffectivePower(gd, gargoyle)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, gargoyle)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Gargoyle ventures for its controller and can choose another dungeon")
    void opponentChoosesMadMage() {
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player2, new CloisterGargoyle());

        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertLife(player2, 21);
    }
}
