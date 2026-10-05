package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Leveler.class, Frogmite.class})
class LevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all cards from its controller's library when it enters")
    void exilesControllerLibraryOnEnter() {
        Card topCard = new Frogmite();
        Card bottomCard = new Frogmite();
        Card opponentCard = new Frogmite();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.castFromHand(player1, new Leveler(), "{5}");

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard, bottomCard);
    }

    @Test
    @DisplayName("Exiles the library as it exists when the enter trigger resolves")
    void exilesLibraryAtResolutionRatherThanEntry() {
        Card originalCard = new Frogmite();
        Card addedCard = new Frogmite();
        Card alreadyExiledCard = new Frogmite();
        harness.setLibrary(player1, List.of(originalCard));
        harness.setExile(player1, List.of(alreadyExiledCard));
        harness.castFromHand(player1, new Leveler(), "{5}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leveler");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(alreadyExiledCard);

        harness.setLibrary(player1, List.of(originalCard, addedCard));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(alreadyExiledCard, originalCard, addedCard);
    }

    @Test
    @DisplayName("Triggers when entering without being cast and exiles the entering controller's library")
    void enteringWithoutCastingExilesOtherControllersLibrary() {
        Card firstPlayerCard = new Frogmite();
        Card secondPlayerCard = new Frogmite();
        harness.setLibrary(player1, List.of(firstPlayerCard));
        harness.setLibrary(player2, List.of(secondPlayerCard));

        harness.enterBattlefieldAndReturn(player2, new Leveler());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstPlayerCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(secondPlayerCard);
        harness.assertOnBattlefield(player2, "Leveler");
    }

    @Test
    @DisplayName("Does not affect the opponent's library when its controller's library is empty")
    void emptyControllerLibraryLeavesOpponentLibraryUntouched() {
        Card opponentCard = new Frogmite();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opponentCard));
        harness.castFromHand(player1, new Leveler(), "{5}");

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
