package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbbotOfKeralKeepTest extends BaseCardTest {

    private Card putOnTop(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{2}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }

    private void castAbbot(Player player) {
        harness.setHand(player, List.of(new AbbotOfKeralKeep()));
        harness.addMana(player, ManaColor.RED, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        // Resolve the enters-the-battlefield trigger put on the stack by the creature resolving.
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles the top card with end-of-turn play permission at normal costs")
    void etbExilesTopCardWithPlayPermission() {
        Card top = putOnTop(player1, "Top");
        Card below = putOnTop(player1, "Below");

        castAbbot(player1);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(below.getId()));
        assertThat(gd.exilePlayPermissions.get(below.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(below.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(below.getId());

        // Only the single top card is exiled.
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(top.getId());
    }

    @Test
    @DisplayName("Exiles nothing when the library is empty")
    void exilesNothingWithEmptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        castAbbot(player1);

        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Play permission is cleared during end-of-turn cleanup")
    void permissionExpiresAtEndOfTurn() {
        Card top = putOnTop(player1, "Top");

        castAbbot(player1);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }
}
