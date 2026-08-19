package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProsperousPirates;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForerunnerOfTheCoalitionTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a Pirate and put it on top of the library")
    void maySearchForPirateToTopOfLibrary() {
        harness.setHand(player1, List.of(new ForerunnerOfTheCoalition()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(new ProsperousPirates(), new VampireInterloper()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.PIRATE));

        harness.getGameService().handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(ProsperousPirates.class);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another Pirate entering makes each opponent lose 1 life")
    void pirateEnteringMakesEachOpponentLoseLife() {
        harness.addToBattlefield(player1, new ForerunnerOfTheCoalition());
        harness.setHand(player1, List.of(new ProsperousPirates()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("A non-Pirate creature entering does not trigger life loss")
    void nonPirateEnteringDoesNotMakeOpponentLoseLife() {
        harness.addToBattlefield(player1, new ForerunnerOfTheCoalition());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void resolveStack() {
        for (int guard = 0; guard < 20 && !gd.stack.isEmpty(); guard++) {
            harness.passBothPriorities();
        }
    }
}
