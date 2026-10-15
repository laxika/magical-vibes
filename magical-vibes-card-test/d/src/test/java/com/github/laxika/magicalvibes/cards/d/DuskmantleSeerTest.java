package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gridlock;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskmantleSeer.class, GrizzlyBears.class, Shock.class, DimirGuildgate.class, Gridlock.class})
class DuskmantleSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals top card, loses life equal to its mana value, then puts it into hand")
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

    @Test
    @DisplayName("A land causes no life loss and X is zero in a library")
    void landAndXManaValues() {
        harness.addToBattlefield(player1, new DuskmantleSeer());
        Card land = new DimirGuildgate();
        Card gridlock = new Gridlock();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(gridlock));
        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2 - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player2.getId())).contains(gridlock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gameLogContains("reveals Dimir Guildgate")).isTrue();
        assertThat(gameLogContains("reveals Gridlock")).isTrue();
    }

    @Test
    @DisplayName("An empty controller library does not prevent the opponent receiving their card")
    void emptyControllerLibraryDoesNotStopOpponent() {
        harness.addToBattlefield(player1, new DuskmantleSeer());
        Card gridlock = new Gridlock();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(gridlock));
        int startingLife1 = gd.getLife(player1.getId());
        int startingLife2 = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife2 - 1);
        assertThat(gd.playerHands.get(player2.getId())).contains(gridlock);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
