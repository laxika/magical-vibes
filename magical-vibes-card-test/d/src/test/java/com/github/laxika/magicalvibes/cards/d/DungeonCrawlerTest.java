package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VeteranDungeoneer;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DungeonCrawler.class, VeteranDungeoneer.class})
class DungeonCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        DungeonCrawler crawler = new DungeonCrawler();
        harness.castFromHand(player1, crawler, "{B}");
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == crawler)
                .findFirst()
                .orElseThrow();
        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May return itself from the graveyard when its controller completes a dungeon")
    void mayReturnFromGraveyardOnDungeonCompletion() {
        DungeonCrawler crawler = new DungeonCrawler();
        harness.setGraveyard(player1, List.of(crawler));
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));

        castVeteranDungeoneer(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playersWhoCompletedDungeon).contains(player1.getId());
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(crawler);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(crawler);
    }

    @Test
    @DisplayName("May decline the dungeon-completion return")
    void mayDeclineReturnFromGraveyard() {
        DungeonCrawler crawler = new DungeonCrawler();
        harness.setGraveyard(player1, List.of(crawler));
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));

        castVeteranDungeoneer(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(crawler);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(crawler);
    }

    @Test
    @DisplayName("An opponent completing a dungeon does not return the crawler")
    void opponentCompletionDoesNotTriggerReturn() {
        DungeonCrawler crawler = new DungeonCrawler();
        harness.setGraveyard(player1, List.of(crawler));
        gd.playerDungeonProgress.put(player2.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));
        harness.forceActivePlayer(player2);

        castVeteranDungeoneer(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playersWhoCompletedDungeon).contains(player2.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(crawler);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(crawler);
    }

    @Test
    @DisplayName("Entering an ordinary dungeon room does not trigger the return")
    void advancingWithoutCompletionDoesNotTriggerReturn() {
        DungeonCrawler crawler = new DungeonCrawler();
        harness.setGraveyard(player1, List.of(crawler));
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));

        castVeteranDungeoneer(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Dark Pool");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        assertThat(gd.playersWhoCompletedDungeon).doesNotContain(player1.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(crawler);
    }

    @Test
    @DisplayName("A pending return cannot find a crawler that left and reentered the graveyard")
    void pendingReturnDoesNotReturnNewGraveyardObject() {
        DungeonCrawler crawler = new DungeonCrawler();
        harness.setGraveyard(player1, List.of(crawler));
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 5));

        castVeteranDungeoneer(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playersWhoCompletedDungeon).contains(player1.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, crawler.getId()));
        Permanent returned = harness.addToBattlefieldAndReturn(player1, crawler);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, returned));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(crawler);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(crawler);
    }

    private void castVeteranDungeoneer(Player player) {
        harness.castFromHand(player, new VeteranDungeoneer(), "{3}{W}");
    }
}
