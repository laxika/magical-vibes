package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraconicFealty.class, Forest.class, GrizzlyBears.class, ShivanDragon.class})
class DraconicFealtyTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card with the greatest mana value")
    void discardsGreatestManaValueCard() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new ShivanDragon()));
        harness.setHand(player1, List.of(new DraconicFealty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(2);

        harness.handleCardChosen(player2, 2);

        harness.assertInGraveyard(player2, "Shivan Dragon");
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Dragon behold also exiles the target player's graveyard")
    void beholdingDragonExilesTargetPlayersGraveyard() {
        ShivanDragon dragon = new ShivanDragon();
        GrizzlyBears handCard = new GrizzlyBears();
        GrizzlyBears graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DraconicFealty(), dragon));
        harness.setHand(player2, List.of(new Forest(), handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithBehold(player1, 0, player2.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(graveyardCard, handCard);
    }

    @Test
    @DisplayName("A land is eligible when it is tied for greatest mana value")
    void discardsLandFromLandOnlyHand() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new DraconicFealty()));
        harness.setHand(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second);
    }
}
