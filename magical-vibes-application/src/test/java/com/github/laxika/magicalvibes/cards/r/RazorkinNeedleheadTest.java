package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorkinNeedlehead.class, GrizzlyBears.class})
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
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        draw(player2);
        resolveTopOfStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when its controller draws a card")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers separately for each card an opponent draws")
    void triggersForEachOpponentDraw() {
        harness.addToBattlefield(player1, new RazorkinNeedlehead());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        draw(player2);
        draw(player2);
        assertThat(gd.stack).hasSize(2);

        resolveTopOfStack();
        resolveTopOfStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
