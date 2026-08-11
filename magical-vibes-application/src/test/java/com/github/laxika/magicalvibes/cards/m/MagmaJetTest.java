package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagmaJetTest extends BaseCardTest {

    @Test
    void dealsDamageThenAllowsScrying() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MagmaJet()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void scryReordersTheLibraryAndFinishesResolving() {
        harness.setHand(player1, List.of(new MagmaJet()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(top1);
        assertThat(gd.playerDecks.get(player1.getId()).get(1)).isSameAs(top0);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Magma Jet");
    }
}
