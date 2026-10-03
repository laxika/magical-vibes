package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomsServoGuards.class, Forest.class, GrizzlyBears.class})
class DoomsServoGuardsTest extends BaseCardTest {

    @Test
    void entersGainsLifeAndMillsTwoCards() {
        List<Card> library = List.of(
                new Forest(), new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new DoomsServoGuards(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(2));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1));
    }

    @Test
    void millsOnlyCardsRemainingInShortLibrary() {
        List<Card> library = List.of(new Forest());
        harness.setLibrary(player1, library);
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new DoomsServoGuards(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(library.get(0));
    }

    @Test
    void gainsLifeEvenWhenLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new DoomsServoGuards(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastAffectsOnlyItsController() {
        List<Card> controllerLibrary = List.of(new DoomsServoGuards(), new DoomsServoGuards());
        List<Card> opponentLibrary = List.of(new DoomsServoGuards());
        harness.setLibrary(player2, controllerLibrary);
        harness.setLibrary(player1, opponentLibrary);
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        harness.enterBattlefieldAndReturn(player2, new DoomsServoGuards());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(controllerLibrary);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
