package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimensionalBreach.class, GrizzlyBears.class, Island.class})
class DimensionalBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all permanents and returns one owned card at each player's upkeep")
    void exilesAllThenReturnsOneOwnedCardEachUpkeep() {
        Card breach = new DimensionalBreach();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(breach));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(breach.getId())).hasSize(2);

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(breach.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        advanceToSecondTurnUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(breach.getId()))
                .extracting(Card::getName)
                .isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Island");
    }

    @Test
    @DisplayName("An upkeep only returns that upkeep player's owned card")
    void upkeepReturnsOnlyActivePlayersOwnedCard() {
        Card breach = new DimensionalBreach();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(breach));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        advanceToSecondTurnUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Island");
    }

    private void advanceToSecondTurnUpkeep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
    }
}
