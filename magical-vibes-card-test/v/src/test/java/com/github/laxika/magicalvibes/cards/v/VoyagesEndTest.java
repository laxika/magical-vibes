package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({VoyagesEnd.class, GrizzlyBears.class, Mountain.class})
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

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a creature controlled by the caster to its owner")
    void returnsStolenCreatureToOwner() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        var scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    @DisplayName("Does not scry when its only target has left the battlefield")
    void doesNotScryWhenTargetLeavesBattlefield() {
        addTargetCreature();
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.setHand(player1, List.of(new VoyagesEnd()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(originalDeck);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Voyage's End");
    }

    @Test
    @DisplayName("Returns the creature even when the caster's library is empty")
    void resolvesWithEmptyLibrary() {
        addTargetCreature();
        harness.setLibrary(player2, List.of());

        castVoyagesEnd();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Voyage's End");
    }

    private void addTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
    }

    private void castVoyagesEnd() {
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
    }
}
