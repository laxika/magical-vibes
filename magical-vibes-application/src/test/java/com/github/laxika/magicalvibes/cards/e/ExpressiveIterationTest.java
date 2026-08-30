package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressiveIterationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one card into hand, one on the bottom, and exiles one playable this turn")
    void distributesThreeCards() {
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card first = deck.get(0);
        Card second = deck.get(1);
        Card third = deck.get(2);
        int initialDeckSize = deck.size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandBottomExileChoice.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(deck).doesNotContain(first, third);
        assertThat(deck).contains(second);
        assertThat(deck.get(deck.size() - 1)).isSameAs(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(third);
        assertThat(gd.exilePlayPermissions.get(third.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(third.getId());
        assertThat(deck).hasSize(initialDeckSize - 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With two library cards, puts one into hand and one on the bottom")
    void distributesTwoCards() {
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        deck.addAll(List.of(first, second));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(deck).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("End-of-turn cleanup removes the exiled card's play permission")
    void permissionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new ExpressiveIteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        Card exiled = gd.playerDecks.get(player1.getId()).get(2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandBottomExile(0, 1));

        assertThat(gd.exilePlayPermissions).containsKey(exiled.getId());
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
    }
}
