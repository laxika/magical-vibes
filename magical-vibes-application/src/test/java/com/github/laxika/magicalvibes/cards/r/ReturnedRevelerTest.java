package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnedRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, each player mills three cards")
    void eachPlayerMillsThreeCardsWhenItDies() {
        List<Card> player1Library = List.of(new Island(), new Island(), new Island(), new Island());
        List<Card> player2Library = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        harness.addToBattlefield(player1, new ReturnedReveler());
        Permanent reveler = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reveler));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Library.get(3));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Library.get(3));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(reveler.getCard(), player1Library.get(0), player1Library.get(1), player1Library.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(player2Library.get(0), player2Library.get(1), player2Library.get(2));
    }
}
