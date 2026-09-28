package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeenDuelist.class, GrizzlyBears.class, Shock.class})
class KeenDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("You and the target opponent reveal, lose life based on the other card, and draw")
    void revealsAndLosesLifeBasedOnOtherCard() {
        harness.addToBattlefield(player1, new KeenDuelist());

        Card ownCard = new GrizzlyBears();
        Card opponentCard = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(ownCard);
        gd.playerDecks.get(player2.getId()).addFirst(opponentCard);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new KeenDuelist());

        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2);
    }
}
