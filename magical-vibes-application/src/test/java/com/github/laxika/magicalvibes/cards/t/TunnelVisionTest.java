package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TunnelVision.class)
class TunnelVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void resolvingPromptsControllerToNameCard() {
        setLibrary(player2, named("Chosen Card"), named("Tail"));
        castTunnelVision();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context())
                .isInstanceOf(ChoiceContext.ChooseNameRevealUntilNamedPutOnTopRestToGraveyardChoice.class);
    }

    @Test
    @DisplayName("Puts the named card on top and the other revealed cards into the target's graveyard")
    void foundCardGoesOnTopAndOtherRevealedCardsGoToGraveyard() {
        Card first = named("First Card");
        Card chosen = named("Chosen Card");
        Card tail = named("Tail");
        setLibrary(player2, first, chosen, tail);

        castTunnelVision();
        harness.handleListChoice(player1, "Chosen Card");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen, tail);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(chosen, tail);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Shuffles the target's library when the named card is not found")
    void missingCardShufflesLibrary() {
        Card first = named("First Card");
        Card second = named("Second Card");
        setLibrary(player2, first, second);

        castTunnelVision();
        harness.handleListChoice(player1, "Tunnel Vision");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castTunnelVision() {
        harness.setHand(player1, List.of(new TunnelVision()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.put(player.getId(), new ArrayList<>(List.of(cards)));
    }

    private static Card named(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        return card;
    }
}
