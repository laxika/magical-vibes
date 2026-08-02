package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeizanPerverterOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("At an opponent's upkeep that player loses 2 life and draws two cards")
    void opponentLosesLifeAndDrawsAtTheirUpkeep() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        harness.setLibrary(player2, List.of(named("Top"), named("Second"), named("Third")));
        harness.setHand(player2, List.of());
        int startingLife = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(handNames(player2)).contains("Top", "Second");
    }

    @Test
    @DisplayName("The controller is also hit at their own upkeep")
    void controllerLosesLifeAndDrawsAtOwnUpkeep() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        harness.setLibrary(player1, List.of(named("Top"), named("Second"), named("Third")));
        harness.setHand(player1, List.of());
        int startingLife = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(handNames(player1)).contains("Top", "Second");
    }

    @Test
    @DisplayName("The opponent's life total is untouched at the controller's upkeep")
    void onlyTheActivePlayerIsAffected() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        harness.setLibrary(player1, List.of(named("A"), named("B"), named("C")));
        harness.setHand(player2, List.of());
        int opponentLife = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(handNames(player2)).isEmpty();
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getName).toList();
    }

    private Card named(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        return card;
    }
}
