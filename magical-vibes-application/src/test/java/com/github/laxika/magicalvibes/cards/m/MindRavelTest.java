package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindRavel.class, Forest.class})
class MindRavelTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new MindRavel()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B}
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Target player discards a card and a draw is scheduled for the caster")
    void targetDiscardsAndSchedulesDraw() {
        prepare();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves for the caster at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        prepare();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The controller can be chosen as the target")
    void controllerCanBeTargeted() {
        Forest discardedCard = new Forest();
        harness.setHand(player1, List.of(new MindRavel(), discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The delayed draw waits for priority at the next upkeep")
    void delayedDrawWaitsForPriority() {
        prepare();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target player with an empty hand discards nothing but the draw is still scheduled")
    void emptyHandDiscardsNothing() {
        harness.setHand(player1, List.of(new MindRavel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The delayed draw triggers only once, even after another upkeep")
    void delayedDrawTriggersOnlyOnce() {
        prepare();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        var chosenCard = gd.playerHands.get(player2.getId()).get(1);
        harness.handleCardChosen(player2, 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(chosenCard);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        int deckAfterDraw = gd.playerDecks.get(player1.getId()).size();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckAfterDraw);
    }

    @Test
    @DisplayName("The caster draws when their own extra turn is the next turn")
    void delayedDrawOnCastersNextTurn() {
        prepare();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        gd.turnNumber++;
        advanceToUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
