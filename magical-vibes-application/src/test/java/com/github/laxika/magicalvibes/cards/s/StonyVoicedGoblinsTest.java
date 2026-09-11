package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonyVoicedGoblins.class, GrizzlyBears.class})
class StonyVoicedGoblinsTest extends BaseCardTest {

    @Test
    @DisplayName("When Stony-Voiced Goblins enters, each opponent discards a card")
    void eachOpponentDiscardsACard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new StonyVoicedGoblins())));
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stony-Voiced Goblins's ETB does nothing when an opponent has no cards")
    void emptyOpponentHand() {
        harness.setHand(player1, new ArrayList<>(List.of(new StonyVoicedGoblins())));
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
