package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GliderKids.class)
class GliderKidsTest extends BaseCardTest {

    @Test
    void enteringBattlefieldStartsScryOne() {
        castGliderKids();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        castGliderKids();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getFirst()).isNotSameAs(originalTop);
        assertThat(deck.getLast()).isSameAs(originalTop);
    }

    @Test
    void scryCanKeepTopCardWithoutChangingEitherLibrary() {
        Card top = new GliderKids();
        Card next = new GliderKids();
        Card opponentsTop = new GliderKids();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opponentsTop));
        castGliderKids();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTop);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryWithEmptyLibraryCompletesWithoutPromptOrDrawing() {
        harness.setLibrary(player1, List.of());
        castGliderKids();

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isInstanceOf(GliderKids.class));
    }

    private void castGliderKids() {
        harness.setHand(player1, List.of(new GliderKids()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
