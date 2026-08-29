package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubbleReadingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and starts scry 2")
    void destroysTargetLandAndStartsScryTwo() {
        harness.addToBattlefield(player2, new Mountain());
        castRubbleReading(harness.getPermanentId(player2, "Mountain"));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        PendingInteraction.Scry interaction = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(interaction).isNotNull();
        assertThat(interaction.cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 can put both cards on the bottom")
    void scryTwoCanPutBothCardsOnBottom() {
        harness.addToBattlefield(player2, new Mountain());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalFirst = deck.get(0);
        Card originalSecond = deck.get(1);

        castRubbleReading(harness.getPermanentId(player2, "Mountain"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(deck.get(deck.size() - 2)).isSameAs(originalSecond);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalFirst);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rubble Reading");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RubbleReading()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRubbleReading(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RubbleReading()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
