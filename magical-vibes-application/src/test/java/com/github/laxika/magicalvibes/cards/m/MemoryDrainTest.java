package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryDrain.class, GrizzlyBears.class})
class MemoryDrainTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell and scries 2")
    void countersTargetSpellAndScriesTwo() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top0, top1);

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(deck).startsWith(top1, top0);
        harness.assertInGraveyard(player2, "Memory Drain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(
                player2, 0, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
