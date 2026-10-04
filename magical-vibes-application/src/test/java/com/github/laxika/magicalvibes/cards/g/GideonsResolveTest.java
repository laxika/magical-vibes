package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonsResolve.class, GideonMartialParagon.class, ThoseWhoServe.class, Opalescence.class})
class GideonsResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gideon's Resolve triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        setupAndCast();

        harness.passBothPriorities(); // resolve enchantment -> ETB may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may finds Gideon, Martial Paragon in graveyard and puts it into hand")
    void acceptingMayFindsInGraveyard() {
        Card gideon = createGideonMartialParagon();
        harness.setGraveyard(player1, List.of(gideon));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Gideon, Martial Paragon");
        harness.assertNotInGraveyard(player1, "Gideon, Martial Paragon");
    }

    @Test
    @DisplayName("Accepting may searches library when not in graveyard")
    void acceptingMaySearchesLibrary() {
        Card gideon = createGideonMartialParagon();
        harness.setLibrary(player1, List.of(gideon));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst()
                .getName()).isEqualTo("Gideon, Martial Paragon");
    }

    @Test
    @DisplayName("Accepting may when Gideon is not in library or graveyard does nothing")
    void acceptingMayWhenNotFound() {
        gd.playerDecks.get(player1.getId()).clear();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may ability does not search")
    void decliningMayDoesNotSearch() {
        Card gideon = createGideonMartialParagon();
        harness.setGraveyard(player1, List.of(gideon));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Gideon, Martial Paragon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gideon's Resolve enters the battlefield after resolving")
    void entersBattlefield() {
        setupAndCast();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gideon's Resolve");
    }

    @Test
    @DisplayName("Own creatures get +1/+1")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new GideonsResolve());
        harness.addToBattlefield(player1, new ThoseWhoServe());

        Permanent vanguard = findPermanent(player1, "Those Who Serve");

        // Those Who Serve is 2/4, with +1/+1 should be 3/5
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's creatures do not get buffed")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new GideonsResolve());
        harness.addToBattlefield(player2, new ThoseWhoServe());

        Permanent opponentVanguard = findPermanent(player2, "Those Who Serve");

        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(4);
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new GideonsResolve(), "{4}{W}");
    }

    private Card createGideonMartialParagon() {
        return new GideonMartialParagon();
    }

    @Test
    void librarySearchPutsChosenCardIntoHand() {
        Card gideon = new GideonMartialParagon();
        harness.setLibrary(player1, List.of(gideon));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(gideon);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayFailToFindInLibrary() {
        Card gideon = new GideonMartialParagon();
        harness.setLibrary(player1, List.of(gideon));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(gideon);
    }

    @Test
    void acceptingSearchDoesNotAutomaticallyTakeGraveyardCopyWhenLibraryAlsoHasOne() {
        Card graveyardGideon = new GideonMartialParagon();
        Card libraryGideon = new GideonMartialParagon();
        harness.setGraveyard(player1, List.of(graveyardGideon));
        harness.setLibrary(player1, List.of(libraryGideon));
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardGideon);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void multipleResolvesStackTheirBoosts() {
        harness.addToBattlefield(player1, new GideonsResolve());
        harness.addToBattlefield(player1, new GideonsResolve());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }
    @Test
    void animatedResolveAlsoGetsItsOwnBoost() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent resolve = harness.addToBattlefieldAndReturn(player1, new GideonsResolve());

        assertThat(gqs.getEffectivePower(gd, resolve)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, resolve)).isEqualTo(6);
    }
}
