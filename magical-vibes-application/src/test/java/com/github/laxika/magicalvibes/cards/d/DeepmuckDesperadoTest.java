package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepmuckDesperado.class, Shock.class})
class DeepmuckDesperadoTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent mills three cards after the controller commits a crime")
    void millsEachOpponentAfterCrime() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        castShockAtOpponent();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(8));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Targeting yourself does not commit a crime")
    void targetingYourselfDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller does not mill their own library")
    void doesNotMillController() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player1, libraryWithCards(5));
        harness.setLibrary(player2, libraryWithCards(5));

        castShockAtOpponent();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's crime does not trigger the ability")
    void opponentCrimeDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName).containsExactly("Shock");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Targeting a permanent you control does not commit a crime")
    void targetingOwnPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Deepmuck Desperado"));

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting an opponent's permanent commits a crime")
    void targetingOpponentPermanentTriggers() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.addToBattlefield(player2, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Deepmuck Desperado"));

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A short library mills all remaining cards")
    void millsShortLibrary() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(2));

        castShockAtOpponent();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Each copy triggers independently for the same crime")
    void eachCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(8));

        castShockAtOpponent();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The ability can trigger again during the opponent's next turn")
    void triggersAgainNextTurn() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(10));
        castShockAtOpponent();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        castShockAtOpponent();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The queued mill trigger survives removal of its source")
    void queuedTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new DeepmuckDesperado());
        harness.setLibrary(player2, libraryWithCards(5));
        var sourceId = harness.getPermanentId(player1, "Deepmuck Desperado");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, sourceId);

        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertNotOnBattlefield(player1, "Deepmuck Desperado");
        harness.assertInGraveyard(player1, "Deepmuck Desperado");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    private void castShockAtOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private List<Card> libraryWithCards(int count) {
        return IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new Shock())
                .toList();
    }
}
