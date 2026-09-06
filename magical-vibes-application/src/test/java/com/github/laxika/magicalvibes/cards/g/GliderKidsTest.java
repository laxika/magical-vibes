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

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        castGliderKids();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getFirst()).isNotSameAs(originalTop);
        assertThat(deck.getLast()).isSameAs(originalTop);
    }

    private void castGliderKids() {
        harness.setHand(player1, List.of(new GliderKids()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
