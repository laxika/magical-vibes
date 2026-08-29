package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountOnLuckTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, exiles the top card with play permission")
    void exilesTopCardWithPlayPermission() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Play permission expires at the end of the turn")
    void playPermissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new CountOnLuck());
        Card top = new Island();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }
}
