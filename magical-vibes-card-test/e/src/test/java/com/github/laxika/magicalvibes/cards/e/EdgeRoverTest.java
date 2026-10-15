package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdgeRover.class, WrathOfGod.class, Forest.class})
class EdgeRoverTest extends BaseCardTest {

    @Test
    @DisplayName("When Edge Rover dies, each player creates a Lander token")
    void deathTriggerCreatesLanderForEachPlayer() {
        harness.addToBattlefield(player1, new EdgeRover());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).hasSize(1);
    }

    @Test
    void eachPlayersLanderCanSearchItsControllersLibrary() {
        createLanders();
        for (var player : List.of(player1, player2)) {
            harness.setLibrary(player, List.of(new EdgeRover(), new Forest()));
            harness.addMana(player, ManaColor.COLORLESS, 2);
            harness.activateAbility(player, 0, null, null);

            assertThat(findPermanents(player, "Lander")).isEmpty();
            harness.passBothPriorities();
            gs.handleInteractionAnswer(gd, player, new InteractionAnswer.LibraryCardChosen(0));

            assertThat(findPermanent(player, "Forest").isTapped()).isTrue();
            assertThat(gd.playerDecks.get(player.getId()))
                    .hasSize(1).allMatch(card -> card instanceof EdgeRover);
        }
    }

    @Test
    void landerCanFailToFindEvenWhenBasicLandIsAvailable() {
        createLanders();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).hasSize(1);
    }

    private void createLanders() {
        harness.addToBattlefield(player1, new EdgeRover());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
