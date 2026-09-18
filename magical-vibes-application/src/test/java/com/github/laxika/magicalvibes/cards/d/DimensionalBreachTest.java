package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimensionalBreach.class, DaruSpiritualist.class, TempleOfTheFalseGod.class})
class DimensionalBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all permanents and returns one owned card at each player's upkeep")
    void exilesAllThenReturnsOneOwnedCardEachUpkeep() {
        Card breach = new DimensionalBreach();
        Card creature = new DaruSpiritualist();
        Card land = new TempleOfTheFalseGod();
        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player2, land);

        harness.castFromHand(player1, breach, "{5}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(breach.getId())).hasSize(2);

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(breach.getId()))
                .containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        advanceToSecondTurnUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(breach.getId()))
                .isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(land);
    }

    @Test
    @DisplayName("An upkeep only returns that upkeep player's owned card")
    void upkeepReturnsOnlyActivePlayersOwnedCard() {
        Card breach = new DimensionalBreach();
        Card creature = new DaruSpiritualist();
        Card land = new TempleOfTheFalseGod();
        harness.addToBattlefield(player1, creature);
        harness.addToBattlefield(player2, land);

        harness.castFromHand(player1, breach, "{5}{W}{W}");
        harness.passBothPriorities();

        advanceToSecondTurnUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(land);
    }

    @Test
    @DisplayName("Each upkeep returns one owned card and offers the active player a choice")
    void eachUpkeepReturnsOneOwnedCardAndOffersChoice() {
        Card breach = new DimensionalBreach();
        Card ownedByPlayer1ControlledByPlayer2 = new DaruSpiritualist();
        ownedByPlayer1ControlledByPlayer2.setOwnerId(player1.getId());
        Card ownedByPlayer1ControlledByPlayer1 = new TempleOfTheFalseGod();
        ownedByPlayer1ControlledByPlayer1.setOwnerId(player1.getId());
        Card ownedByPlayer2ControlledByPlayer1 = new DaruSpiritualist();
        ownedByPlayer2ControlledByPlayer1.setOwnerId(player2.getId());

        harness.addToBattlefield(player2, ownedByPlayer1ControlledByPlayer2);
        harness.addToBattlefield(player1, ownedByPlayer1ControlledByPlayer1);
        harness.addToBattlefield(player1, ownedByPlayer2ControlledByPlayer1);

        harness.castFromHand(player1, breach, "{5}{W}{W}");
        harness.passBothPriorities();

        advanceToSecondTurnUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards())
                .containsExactlyInAnyOrder(ownedByPlayer1ControlledByPlayer2, ownedByPlayer1ControlledByPlayer1);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(ownedByPlayer1ControlledByPlayer2.getId(),
                        ownedByPlayer1ControlledByPlayer1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownedByPlayer1ControlledByPlayer2.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(ownedByPlayer1ControlledByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(breach.getId()))
                .containsExactly(ownedByPlayer1ControlledByPlayer1, ownedByPlayer2ControlledByPlayer1);

        advanceToSecondTurnUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(ownedByPlayer2ControlledByPlayer1);
    }

    private void advanceToSecondTurnUpkeep(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
    }
}
