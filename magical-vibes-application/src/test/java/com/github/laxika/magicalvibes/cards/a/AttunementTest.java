package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Attunement.class, Plains.class, Forest.class, Island.class, Mountain.class})
class AttunementTest extends BaseCardTest {

    @Test
    void returnsToHandThenDrawsThreeAndDiscardsFour() {
        harness.addToBattlefield(player1, new Attunement());
        harness.setHand(player1, List.of(new Plains()));
        harness.setLibrary(player1, List.of(
                new Forest(),
                new Island(),
                new Mountain()
        ));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Attunement");
        harness.assertInHand(player1, "Attunement");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, firstNonAttunementCardIndex());
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Attunement");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Forest", "Island", "Mountain");
    }

    @Test
    void canDiscardReturnedSourceWhenNoOtherCardsAreInHand() {
        harness.addToBattlefield(player1, new Attunement());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(),
                new Island(),
                new Mountain()
        ));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, firstNonAttunementCardIndex());
        }
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Attunement", "Forest", "Island", "Mountain");
    }

    private int firstNonAttunementCardIndex() {
        List<Card> hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (!hand.get(i).getName().equals("Attunement")) {
                return i;
            }
        }
        throw new AssertionError("Attunement should remain in hand");
    }
}
