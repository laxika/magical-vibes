package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeizanPerverterOfTruth.class, DevotedRetainer.class, HarshDeceiver.class,
        SireOfTheStorm.class})
class SeizanPerverterOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("At an opponent's upkeep that player loses 2 life and draws two cards")
    void opponentLosesLifeAndDrawsAtTheirUpkeep() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        harness.setLibrary(player2, drawLibrary());
        harness.setHand(player2, List.of());
        int startingLife = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(handNames(player2)).containsExactlyInAnyOrder("Devoted Retainer", "Harsh Deceiver");
    }

    @Test
    @DisplayName("The controller is also hit at their own upkeep")
    void controllerLosesLifeAndDrawsAtOwnUpkeep() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        harness.setLibrary(player1, drawLibrary());
        harness.setHand(player1, List.of());
        int startingLife = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(handNames(player1)).containsExactlyInAnyOrder("Devoted Retainer", "Harsh Deceiver");
    }

    @Test
    @DisplayName("The opponent's life total is untouched at the controller's upkeep")
    void onlyTheActivePlayerIsAffected() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        harness.setLibrary(player1, drawLibrary());
        harness.setHand(player2, List.of());
        int opponentLife = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(handNames(player2)).isEmpty();
    }

    @Test
    @DisplayName("Each Seizan trigger applies separately during the active player's upkeep")
    void multipleSeizansEachTrigger() {
        addCreatureReady(player1, new SeizanPerverterOfTruth());
        addCreatureReady(player2, new SeizanPerverterOfTruth());
        harness.setLibrary(player2, drawLibrary());
        harness.setHand(player2, List.of());
        int startingLife = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 4);
        assertThat(handNames(player2)).hasSize(4);
    }

    private List<Card> drawLibrary() {
        return List.of(new DevotedRetainer(), new HarshDeceiver(), new SireOfTheStorm(),
                new DevotedRetainer());
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getName).toList();
    }
}
