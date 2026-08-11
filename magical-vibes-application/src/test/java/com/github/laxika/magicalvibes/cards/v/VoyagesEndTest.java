package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoyagesEndTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature and starts scry 1")
    void returnsTargetCreatureAndStartsScry() {
        addTargetCreature();
        castVoyagesEnd();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Scry 1 keeps the top card on top")
    void scryKeepsTopCardOnTop() {
        addTargetCreature();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card originalTop = deck.get(0);
        castVoyagesEnd();

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.get(0)).isSameAs(originalTop);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Voyage's End");
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom")
    void scryPutsTopCardOnBottom() {
        addTargetCreature();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card originalTop = deck.get(0);
        castVoyagesEnd();

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Voyage's End");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        var mountain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mountain"))
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
    }

    private void castVoyagesEnd() {
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
    }
}
