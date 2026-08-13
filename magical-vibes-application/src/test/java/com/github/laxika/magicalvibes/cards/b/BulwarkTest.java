package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BulwarkTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the controller's hand-size advantage")
    void dealsDamageEqualToHandSizeDifference() {
        harness.addToBattlefield(player1, new Bulwark());
        harness.setHand(player1, cards(5));
        harness.setHand(player2, cards(2));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has at least as many cards in hand")
    void dealsNoDamageWhenOpponentHasAtLeastAsManyCards() {
        harness.addToBattlefield(player1, new Bulwark());
        harness.setHand(player1, cards(2));
        harness.setHand(player2, cards(4));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Triggers during the controller's upkeep")
    void triggersDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new Bulwark());
        harness.setHand(player1, cards(1));
        harness.setHand(player2, List.of());
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private List<Card> cards(int count) {
        return Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }
}
