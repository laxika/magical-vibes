package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasInfluence.class, LilianaDeathWielder.class, DuneBeetle.class})
class LilianasInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on each creature you don't control")
    void putsMinusOneOnOpponentCreaturesOnly() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(theirs.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(mine.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Resolving prompts may search for Liliana, Death Wielder")
    void resolvingPromptsMaySearch() {
        setupAndCast();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Liliana, Death Wielder in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card liliana = createLilianaDeathWielder();
        harness.setGraveyard(player1, List.of(liliana));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Liliana, Death Wielder");
        harness.assertNotInGraveyard(player1, "Liliana, Death Wielder");
    }

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card liliana = createLilianaDeathWielder();
        harness.setLibrary(player1, List.of(liliana));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst()
                .getName()).isEqualTo("Liliana, Death Wielder");
    }

    @Test
    @DisplayName("Declining may ability does not search")
    void decliningMayDoesNotSearch() {
        Card liliana = createLilianaDeathWielder();
        harness.setGraveyard(player1, List.of(liliana));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Liliana, Death Wielder");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library selection puts exactly the chosen Liliana into hand")
    void librarySelectionMovesChosenCopyToHand() {
        Card chosen = new LilianaDeathWielder();
        Card other = new LilianaDeathWielder();
        Card filler = new DuneBeetle();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(chosen, other, filler));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(other, filler);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library search may fail to find even when Liliana is present")
    void librarySearchCanFailToFind() {
        Card liliana = new LilianaDeathWielder();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(liliana));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(liliana);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting a search must not force a graveyard copy when a library copy exists")
    void doesNotAutomaticallyTakeGraveyardCopyInsteadOfAllowingLibrarySearch() {
        Card graveyardCopy = new LilianaDeathWielder();
        Card libraryCopy = new LilianaDeathWielder();
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setLibrary(player1, List.of(libraryCopy));
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCopy);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Counters affect every opposing creature but not an opposing planeswalker")
    void countersExcludeNoncreaturesAndStillApplyWhenSearchIsDeclined() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaDeathWielder());
        setupAndCast();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new LilianasInfluence(), "{4}{B}{B}");
    }

    private Card createLilianaDeathWielder() {
        return new LilianaDeathWielder();
    }
}
