package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticSkyfish.class})
class MysticSkyfishTest extends BaseCardTest {

    @Test
    void secondDrawGrantsFlyingOnlyAfterResolutionAndOnlyOnce() {
        Permanent fish = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());
        stockLibrary(player1);

        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();

        draw(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isTrue();

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void onlyControllerDrawsCountIncludingOnOpponentTurn() {
        Permanent fish = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());
        harness.forceActivePlayer(player2);
        stockLibrary(player1);
        stockLibrary(player2);

        draw(player2);
        draw(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();

        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isTrue();
    }

    @Test
    void firstDrawBeforeEnteringStillCounts() {
        stockLibrary(player1);
        draw(player1);
        Permanent fish = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());

        draw(player1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isTrue();
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        stockLibrary(player1);
        draw(player1);
        draw(player1);
        Permanent fish = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();
    }

    @Test
    void eachSkyfishGrantsFlyingOnlyToItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MysticSkyfish());
        stockLibrary(player1);

        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingExpiresAndDrawCountResetsForNextTurn() {
        Permanent fish = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());
        stockLibrary(player1);
        draw(player1);
        draw(player1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();
        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isTrue();
    }

    @Test
    void triggerDoesNotGrantFlyingToNewSkyfishAfterSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());
        stockLibrary(player1);
        draw(player1);
        draw(player1);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new MysticSkyfish());

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, replacement, Keyword.FLYING)).isFalse();
    }

    private void stockLibrary(Player player) {
        harness.setLibrary(player, List.of(new MysticSkyfish(), new MysticSkyfish(),
                new MysticSkyfish(), new MysticSkyfish(), new MysticSkyfish(), new MysticSkyfish()));
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
