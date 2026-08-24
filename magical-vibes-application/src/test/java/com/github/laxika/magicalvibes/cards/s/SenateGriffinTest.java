package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SenateGriffinTest extends BaseCardTest {

    @Test
    void enteringOffersScryOne() {
        harness.setHand(player1, List.of(new SenateGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void enteringCanPutTopCardOnBottom() {
        harness.setHand(player1, List.of(new SenateGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.getFirst();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(
                gd,
                player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0))
        );

        assertThat(deck.getLast()).isSameAs(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
