package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeenDuelist.class, GrizzlyBears.class, Shock.class})
class KeenDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep makes you and a target opponent lose life from the other revealed card")
    void revealsBothCardsAndUsesOppositeManaValues() {
        harness.addToBattlefield(player1, new KeenDuelist());
        Card player1Card = new GrizzlyBears();
        Card player2Card = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(player1Card);
        gd.playerDecks.get(player2.getId()).addFirst(player2Card);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Card);
    }

    @Test
    @DisplayName("The upkeep trigger cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new KeenDuelist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
