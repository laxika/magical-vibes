package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SparkJoltTest extends BaseCardTest {

    @Test
    void dealsDamageThenAllowsScrying() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void scryReordersTheLibraryAndFinishesResolving() {
        harness.setHand(player1, List.of(new SparkJolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.get(0);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isNotSameAs(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spark Jolt");
    }
}
