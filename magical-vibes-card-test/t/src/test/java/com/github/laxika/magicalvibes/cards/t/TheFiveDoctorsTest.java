package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninArbiter;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFiveDoctors.class, TheFourthDoctor.class, TheTenthDoctor.class, Forest.class, LeoninArbiter.class,
        PsychogenicProbe.class})
class TheFiveDoctorsTest extends BaseCardTest {

    @Test
    @DisplayName("Without the library, only the graveyard's Doctors are offered and the library isn't searched")
    void searchingOnlyTheGraveyardOffersOnlyItsDoctors() {
        Card libraryDoctor = new TheTenthDoctor();
        Card graveyardDoctor = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(libraryDoctor));
        harness.setGraveyard(player1, List.of(graveyardDoctor));
        harness.setHand(player1, List.of(new TheFiveDoctors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class)
                .validCardIds()).containsExactly(graveyardDoctor.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardDoctor.getId()));

        harness.assertOnBattlefield(player1, "The Fourth Doctor");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryDoctor);
    }

    @Test
    @DisplayName("May choose zero doctors even when doctors are available")
    void mayChooseZeroDoctorsEvenWhenDoctorsAreAvailable() {
        Card libraryDoctor = new TheTenthDoctor();
        Card graveyardDoctor = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(libraryDoctor));
        harness.setGraveyard(player1, List.of(graveyardDoctor));
        harness.setHand(player1, List.of(new TheFiveDoctors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryDoctor);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardDoctor);
        harness.assertNotOnBattlefield(player1, "The Tenth Doctor");
        harness.assertNotOnBattlefield(player1, "The Fourth Doctor");
        harness.assertInGraveyard(player1, "The Five Doctors");
    }

    @Test
    @DisplayName("Limits the selection to five Doctors and excludes the opponent's zones")
    void limitsSelectionToFiveDoctorsAndExcludesOpponentsZones() {
        List<Card> doctors = IntStream.range(0, 6)
                .mapToObj(i -> (Card) new TheTenthDoctor()).toList();
        Card opposingLibraryDoctor = new TheFourthDoctor();
        Card opposingGraveyardDoctor = new TheFourthDoctor();
        harness.setLibrary(player1, doctors.subList(0, 3));
        harness.setGraveyard(player1, doctors.subList(3, 6));
        harness.setLibrary(player2, List.of(opposingLibraryDoctor));
        harness.setGraveyard(player2, List.of(opposingGraveyardDoctor));
        harness.setHand(player1, List.of(new TheFiveDoctors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                doctors.stream().map(Card::getId).toList());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                doctors.stream().map(Card::getId).toList())).isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1,
                doctors.subList(0, 5).stream().map(Card::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(doctors.subList(0, 5));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(doctors.get(5));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingLibraryDoctor);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingGraveyardDoctor);
    }

    @Test
    @DisplayName("Searches up to five doctors from library and graveyard into hand")
    void searchesUpToFiveDoctorsFromLibraryAndGraveyardIntoHand() {
        Card libraryDoctor = new TheTenthDoctor();
        Card graveyardDoctor = new TheFourthDoctor();
        Card nonDoctor = new Forest();
        harness.setLibrary(player1, List.of(nonDoctor, libraryDoctor));
        harness.setGraveyard(player1, List.of(graveyardDoctor));
        harness.setHand(player1, List.of(new TheFiveDoctors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(5);
        assertThat(choice.validCardIds()).containsExactly(libraryDoctor.getId(), graveyardDoctor.getId());

        harness.handleMultipleCardsChosen(player1, List.of(libraryDoctor.getId(), graveyardDoctor.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(libraryDoctor, graveyardDoctor);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDoctor);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardDoctor);
    }

    @Test
    @DisplayName("Kicked search puts selected doctors onto the battlefield")
    void kickedSearchPutsSelectedDoctorsOntoTheBattlefield() {
        Card libraryDoctor = new TheTenthDoctor();
        Card graveyardDoctor = new TheFourthDoctor();
        Card nonDoctor = new Forest();
        harness.setLibrary(player1, List.of(nonDoctor, libraryDoctor));
        harness.setGraveyard(player1, List.of(graveyardDoctor));
        harness.setHand(player1, List.of(new TheFiveDoctors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(libraryDoctor.getId(), graveyardDoctor.getId()));

        harness.assertOnBattlefield(player1, "The Tenth Doctor");
        harness.assertOnBattlefield(player1, "The Fourth Doctor");
        harness.assertNotInGraveyard(player1, "The Fourth Doctor");
        harness.assertInGraveyard(player1, "The Five Doctors");
    }

    @Test
    @DisplayName("Taking no Doctor while library searches are prevented leaves the library unshuffled")
    void preventedLibrarySearchDoesNotShuffle() {
        Card graveyardDoctor = new TheFourthDoctor();
        harness.setLibrary(player1, List.of(new TheTenthDoctor()));
        harness.setGraveyard(player1, List.of(graveyardDoctor));
        harness.addToBattlefield(player2, new LeoninArbiter());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TheFiveDoctors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(graveyardDoctor.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
