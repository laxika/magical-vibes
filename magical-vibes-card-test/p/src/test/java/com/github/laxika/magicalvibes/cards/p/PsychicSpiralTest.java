package com.github.laxika.magicalvibes.cards.p;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PsychicSpiral.class, Island.class, CentaurHealer.class})
class PsychicSpiralTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles controller's graveyard into library and mills target player that many cards")
    void shufflesGraveyardAndMillsThatMany() {
        harness.setGraveyard(player1, List.of(new Island(), new Island(), new CentaurHealer()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Only Psychic Spiral itself remains — it hits the graveyard after resolving.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore + 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills nothing when the controller's graveyard is empty")
    void millsNothingWithEmptyGraveyard() {
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting yourself shuffles the graveyard away first, then mills into the empty graveyard")
    void canTargetSelf() {
        harness.setGraveyard(player1, List.of(new Island(), new Island()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        // 2 shuffled in, 2 milled back out — the milled cards are the only graveyard contents
        // besides Psychic Spiral itself.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        Card spiral = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c instanceof PsychicSpiral).findFirst().orElseThrow();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(spiral);
    }

    @Test
    @DisplayName("Mills only the available cards when the target's library is too small")
    void millsAllRemainingCardsFromShortLibrary() {
        Card first = new Island();
        Card second = new CentaurHealer();
        harness.setLibrary(player2, List.of(first, second));
        harness.setGraveyard(player1, List.of(new Island(), new Island(), new CentaurHealer()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore + 3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement()
                .isInstanceOf(PsychicSpiral.class);
    }

    @Test
    @DisplayName("Counts the cards in the graveyard at resolution rather than at casting")
    void countsGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of(new Island()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, player2.getId());
        harness.setGraveyard(player1, List.of(new Island(), new Island(), new CentaurHealer()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore + 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement()
                .isInstanceOf(PsychicSpiral.class);
    }

    @Test
    @DisplayName("Does not shuffle the graveyard when its only target becomes illegal")
    void illegalTargetPreventsBothInstructions() {
        Card graveyardCard = new CentaurHealer();
        harness.setGraveyard(player1, List.of(graveyardCard));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();
        PsychicSpiral spiral = new PsychicSpiral();
        harness.setHand(player1, List.of(spiral));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, player2.getId());
        gd.playersWithShroudThisTurn.add(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard, spiral);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
