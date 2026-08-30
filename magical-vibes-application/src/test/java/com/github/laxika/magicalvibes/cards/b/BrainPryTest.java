package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrainPry.class, Forest.class, GrizzlyBears.class, Shock.class})
class BrainPryTest extends BaseCardTest {

    @Test
    @DisplayName("The target chooses one matching card to discard")
    void targetChoosesOneMatchingCard() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card shock = new Shock();
        cast(new ArrayList<>(List.of(firstBears, secondBears, shock)));

        harness.handleListChoice(player1, "Grizzly Bears");

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstBears, shock);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondBears);
    }

    @Test
    @DisplayName("Draws a card when the named card is absent from the target's hand")
    void drawsWhenNamedCardIsAbsent() {
        Shock drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BrainPry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Shock");

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void cast(List<Card> targetHand) {
        harness.setHand(player1, List.of(new BrainPry()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
