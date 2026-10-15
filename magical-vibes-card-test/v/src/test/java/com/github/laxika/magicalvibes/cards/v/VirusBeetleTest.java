package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirusBeetle.class})
class VirusBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("When Virus Beetle enters, each opponent discards a card")
    void eachOpponentDiscardsACard() {
        VirusBeetle discarded = new VirusBeetle();
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.castFromHand(player1, new VirusBeetle(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Virus Beetle");
    }

    @Test
    @DisplayName("Virus Beetle's ETB does nothing when an opponent has no cards")
    void emptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        harness.castFromHand(player1, new VirusBeetle(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCardAndControllerDoesNotDiscard() {
        VirusBeetle controllerCard = new VirusBeetle();
        VirusBeetle retained = new VirusBeetle();
        VirusBeetle discarded = new VirusBeetle();
        harness.setHand(player1, List.of(new VirusBeetle(), controllerCard));
        harness.setHand(player2, List.of(retained, discarded));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
