package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WreckageWickerfolk.class})
class WreckageWickerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 2")
    void entersWithSurveilTwo() {
        GameData gd = harness.getGameData();
        Card top0 = new WreckageWickerfolk();
        Card top1 = new WreckageWickerfolk();
        harness.setLibrary(player1, List.of(top0, top1));

        harness.setHand(player1, List.of(new WreckageWickerfolk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Surveil 2 can put both cards into the graveyard")
    void surveilTwoPutsCardsIntoGraveyard() {
        GameData gd = harness.getGameData();
        Card top0 = new WreckageWickerfolk();
        Card top1 = new WreckageWickerfolk();
        harness.setLibrary(player1, List.of(top0, top1));
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.setHand(player1, List.of(new WreckageWickerfolk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top0, top1);
    }

    @Test
    @DisplayName("Surveil can keep both cards in reverse order above the rest of the library")
    void keepsBothCardsInChosenOrder() {
        Card first = new WreckageWickerfolk();
        Card second = new WreckageWickerfolk();
        Card third = new WreckageWickerfolk();
        harness.setLibrary(player1, List.of(first, second, third));

        castAndResolveWickerfolk();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil can keep one card and put the other into its controller's graveyard")
    void splitsCardsBetweenLibraryAndGraveyard() {
        Card first = new WreckageWickerfolk();
        Card second = new WreckageWickerfolk();
        Card third = new WreckageWickerfolk();
        harness.setLibrary(player1, List.of(first, second, third));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        castAndResolveWickerfolk();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second, third);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }

    @Test
    @DisplayName("Surveil 2 with a one-card library looks at only that card")
    void surveilsOnlyAvailableCard() {
        Card onlyCard = new WreckageWickerfolk();
        harness.setLibrary(player1, List.of(onlyCard));

        castAndResolveWickerfolk();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without requesting a choice")
    void emptyLibraryNeedsNoChoice() {
        harness.setLibrary(player1, List.of());

        castAndResolveWickerfolk();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wreckage Wickerfolk");
    }

    private void castAndResolveWickerfolk() {
        harness.setHand(player1, List.of(new WreckageWickerfolk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
