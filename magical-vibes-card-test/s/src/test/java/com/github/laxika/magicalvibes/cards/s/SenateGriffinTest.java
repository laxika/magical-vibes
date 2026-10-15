package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SenateGriffin.class})
class SenateGriffinTest extends BaseCardTest {

    @Test
    void enteringOffersScryOne() {
        harness.setHand(player1, List.of(new SenateGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(
                gd,
                player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0))
        );

        assertThat(deck.getLast()).isSameAs(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringCanKeepTopCardWithoutDrawingIt() {
        SenateGriffin topCard = new SenateGriffin();
        SenateGriffin nextCard = new SenateGriffin();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new SenateGriffin()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithEmptyLibraryCompletesWithoutAnInteraction() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SenateGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Senate Griffin");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
