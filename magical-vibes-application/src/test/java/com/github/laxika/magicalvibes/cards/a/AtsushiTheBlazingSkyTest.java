package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtsushiTheBlazingSky.class, Forest.class, CoilingStalker.class})
class AtsushiTheBlazingSkyTest extends BaseCardTest {

    private static final String PLAY_MODE =
            "Exile the top two cards of your library. Until the end of your next turn, you may play those cards.";
    private static final String TREASURE_MODE = "Create three Treasure tokens";

    @Test
    void deathTriggerExilesTopTwoCardsAndGrantsPlayPermission() {
        Card forest = new Forest();
        Card creature = new CoilingStalker();
        harness.setLibrary(player1, List.of(forest, creature));
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, PLAY_MODE);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(forest, creature);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(forest.getId(), player1.getId())
                .containsEntry(creature.getId(), player1.getId());
    }

    @Test
    void deathTriggerCreatesThreeTreasureTokens() {
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, TREASURE_MODE);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    void exileModeWithEmptyLibraryDoesNotCreateTreasures() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, PLAY_MODE);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void exileModeWithOneCardAllowsPlayingThatLand() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, PLAY_MODE);
        harness.passBothPriorities();
        harness.castFromExile(player1, forest.getId());

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(forest);
    }

    @Test
    void exiledCreatureCanBeCastByPayingItsManaCost() {
        Card creature = new CoilingStalker();
        harness.setLibrary(player1, List.of(creature));
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, PLAY_MODE);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Coiling Stalker")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    void playPermissionLastsThroughNextTurnAndThenExpires() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());

        killAtsushi();
        harness.handleListChoice(player1, PLAY_MODE);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(forest.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(forest.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(forest.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest);
    }

    private void killAtsushi() {
        Permanent atsushi = findPermanent(player1, "Atsushi, the Blazing Sky");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, atsushi));
        harness.passBothPriorities();
    }
}
