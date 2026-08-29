package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheStoneBrainTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to four named cards and the opponent draws for hand cards exiled")
    void exilesUpToFourAndDrawsForHandExiles() {
        List<Card> hand = new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        List<Card> graveyard = new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears()));
        List<Card> library = new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new LightningBolt()));
        harness.setHand(player2, hand);
        harness.setGraveyard(player2, graveyard);
        harness.setLibrary(player2, library);
        harness.addToBattlefield(player1, new TheStoneBrain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");

        PendingInteraction.MultiZoneExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(
                hand.get(0).getId(), hand.get(1).getId(), graveyard.get(0).getId(), graveyard.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsOnly("Grizzly Bears")
                .hasSize(4);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Lightning Bolt")
                .hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("The Stone Brain");
    }
}
