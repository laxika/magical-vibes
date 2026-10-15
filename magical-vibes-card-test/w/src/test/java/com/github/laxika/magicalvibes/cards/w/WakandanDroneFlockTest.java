package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakandanDroneFlock.class})
class WakandanDroneFlockTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesTwo() {
        List<Card> library = List.of(new WakandanDroneFlock(), new WakandanDroneFlock(), new WakandanDroneFlock());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WakandanDroneFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(library.get(0), library.get(1));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(2), library.get(0));
    }

    @Test
    void canReorderBothCardsOnTop() {
        List<Card> library = List.of(new WakandanDroneFlock(), new WakandanDroneFlock(), new WakandanDroneFlock());
        harness.setLibrary(player1, library);
        castAndResolveEnterTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(0), library.get(2));
    }

    @Test
    void canReorderBothCardsOnBottom() {
        List<Card> library = List.of(new WakandanDroneFlock(), new WakandanDroneFlock(), new WakandanDroneFlock());
        harness.setLibrary(player1, library);
        castAndResolveEnterTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(1), library.get(0));
    }

    @Test
    void scriesOnlyAvailableCardAndLeavesOpponentsLibraryAlone() {
        Card onlyCard = new WakandanDroneFlock();
        List<Card> opponentLibrary = List.of(new WakandanDroneFlock(), new WakandanDroneFlock());
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setLibrary(player2, opponentLibrary);
        castAndResolveEnterTrigger();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }

    @Test
    void emptyLibraryDoesNotRequireScryChoice() {
        harness.setLibrary(player1, List.of());
        castAndResolveEnterTrigger();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Wakandan Drone Flock");
    }

    private void castAndResolveEnterTrigger() {
        harness.setHand(player1, List.of(new WakandanDroneFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
