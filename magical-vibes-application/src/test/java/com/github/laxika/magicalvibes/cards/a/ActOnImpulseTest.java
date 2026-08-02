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
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActOnImpulseTest extends BaseCardTest {

    private Card putOnTop(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{4}{R}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }

    private void castActOnImpulse(Player player) {
        harness.setHand(player, List.of(new ActOnImpulse()));
        harness.addMana(player, ManaColor.RED, 3);
        harness.castSorcery(player, 0, (UUID) null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles the top three cards with end-of-turn play permission")
    void exilesTopThreeWithPlayPermission() {
        Card third = putOnTop(player1, "Third");
        Card second = putOnTop(player1, "Second");
        Card first = putOnTop(player1, "First");

        castActOnImpulse(player1);

        for (Card card : List.of(first, second, third)) {
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getId().equals(card.getId()));
            assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(card.getId());
            // Normal costs and timing — never a free play.
            assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(card.getId());
        }
    }

    @Test
    @DisplayName("Exiles only what is left when the library has fewer than three cards")
    void exilesWholeLibraryWhenShort() {
        gd.playerDecks.get(player1.getId()).clear();
        Card only = putOnTop(player1, "Only");

        castActOnImpulse(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsKey(only.getId());
    }

    @Test
    @DisplayName("Play permission is cleared during end-of-turn cleanup")
    void permissionExpiresAtEndOfTurn() {
        Card first = putOnTop(player1, "First");
        putOnTop(player1, "Second");
        putOnTop(player1, "Third");

        castActOnImpulse(player1);
        assertThat(gd.exilePlayPermissions).containsKey(first.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
    }
}
