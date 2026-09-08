package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrennsResolve.class, Shock.class, Forest.class})
class WrennsResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top two cards and grants play permission until the end of the next turn")
    void exilesTopTwoCardsAndGrantsPlayPermission() {
        Card first = new Shock();
        Card second = new Forest();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new WrennsResolve()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(first.getId(), gd.turnNumber + 2)
                .containsEntry(second.getId(), gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Play permission expires at the end of the next turn")
    void playPermissionExpiresAtEndOfNextTurn() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new WrennsResolve()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        int expireTurn = gd.exilePlayPermissionsExpireAtTurnEnd.get(top.getId());
        gd.turnNumber = expireTurn - 1;
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        gd.turnNumber = expireTurn;
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }
}
