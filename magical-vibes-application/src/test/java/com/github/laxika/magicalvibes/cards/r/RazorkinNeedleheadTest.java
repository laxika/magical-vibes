package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorkinNeedlehead.class})
class RazorkinNeedleheadTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllersTurn() {
        Permanent needlehead = addCreatureReady(player1, new RazorkinNeedlehead());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, needlehead, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during an opponent's turn")
    void doesNotHaveFirstStrikeDuringOpponentsTurn() {
        Permanent needlehead = addCreatureReady(player1, new RazorkinNeedlehead());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, needlehead, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Deals 1 damage whenever an opponent draws a card")
    void dealsDamageOnOpponentDraw() {
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.setLibrary(player2, List.of(new RazorkinNeedlehead()));
        harness.setLife(player2, 20);

        draw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when its controller draws a card")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.setLibrary(player1, List.of(new RazorkinNeedlehead()));
        harness.setLife(player1, 20);

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers separately for each card an opponent draws")
    void triggersForEachOpponentDraw() {
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.setLibrary(player2, List.of(new RazorkinNeedlehead(), new RazorkinNeedlehead()));
        harness.setLife(player2, 20);

        draw(player2);
        draw(player2);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Each Needlehead triggers independently for an opponent's draw")
    void multipleCopiesEachDealDamage() {
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.setLibrary(player2, List.of(new RazorkinNeedlehead()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        draw(player2);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A draw trigger still deals damage after Needlehead dies")
    void triggerResolvesAfterSourceDies() {
        Permanent needlehead = harness.addToBattlefieldAndReturn(player1, new RazorkinNeedlehead());
        harness.setLibrary(player2, List.of(new RazorkinNeedlehead()));
        harness.setLife(player2, 20);

        draw(player2);
        needlehead.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("First strike follows the active player as turns change")
    void firstStrikeChangesWithActivePlayer() {
        Permanent needlehead = harness.addToBattlefieldAndReturn(player2, new RazorkinNeedlehead());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, needlehead, Keyword.FIRST_STRIKE)).isFalse();
        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, needlehead, Keyword.FIRST_STRIKE)).isTrue();
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, needlehead, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
