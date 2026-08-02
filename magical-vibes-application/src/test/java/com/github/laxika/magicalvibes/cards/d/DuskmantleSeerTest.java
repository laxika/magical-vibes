package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuskmantleSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals top card, loses life equal to its mana value, then draws it")
    void eachPlayerRevealsLosesLifeAndPutsCardInHand() {
        harness.addToBattlefield(player1, new DuskmantleSeer());

        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        gd.playerDecks.get(player2.getId()).addFirst(shock);

        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1 - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2 - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player2.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Player with an empty library is skipped without life loss")
    void emptyLibraryIsSkipped() {
        harness.addToBattlefield(player1, new DuskmantleSeer());

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        gd.playerDecks.get(player2.getId()).clear();

        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1 - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new DuskmantleSeer());

        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2);
    }
}
