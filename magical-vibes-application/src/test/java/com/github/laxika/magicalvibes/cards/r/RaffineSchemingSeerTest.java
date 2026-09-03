package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaffineSchemingSeer.class, GrizzlyBears.class, Mountain.class})
class RaffineSchemingSeerTest extends BaseCardTest {

    @Test
    void attacksMakeTargetAttackerConniveForEachAttacker() {
        Permanent raffine = addCreatureReady(player1, new RaffineSchemingSeer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears()));

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(attacker.getId(), otherAttacker.getId())
                .doesNotContain(raffine.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        discardByName("Mountain");
        discardByName("Grizzly Bears");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
