package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtsushiTheBlazingSky.class, Forest.class, GrizzlyBears.class})
class AtsushiTheBlazingSkyTest extends BaseCardTest {

    private static final String PLAY_MODE =
            "Exile the top two cards of your library. Until the end of your next turn, you may play those cards.";
    private static final String TREASURE_MODE = "Create three Treasure tokens";

    @Test
    void deathTriggerExilesTopTwoCardsAndGrantsPlayPermission() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, PLAY_MODE);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest, bears);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(forest.getId(), player1.getId())
                .containsEntry(bears.getId(), player1.getId());
    }

    @Test
    void deathTriggerCreatesThreeTreasureTokens() {
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    private void killAtsushi() {
        Permanent atsushi = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AtsushiTheBlazingSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, atsushi));
        harness.passBothPriorities();
    }
}
