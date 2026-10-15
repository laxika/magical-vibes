package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaGranger.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class})
class YavimayaGrangerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may prompt")
    void enteringTheBattlefieldCreatesMayPrompt() {
        setupAndCast();

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability puts a chosen basic land onto the battlefield tapped")
    void acceptingMayPutsChosenBasicLandOntoBattlefieldTapped() {
        setupAndCast();
        setupLibraryWithBasicLands();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Forest", "Island");
        String chosenName = offered.getFirst().getName();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = findPermanent(player1, chosenName);
        assertThat(chosenLand.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithBasicLands();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining echo sacrifices Yavimaya Granger at its next upkeep")
    void decliningEchoSacrificesYavimayaGranger() {
        castAndDeclineLandSearch();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Yavimaya Granger");
        harness.assertInGraveyard(player1, "Yavimaya Granger");
    }

    @Test
    @DisplayName("Paying echo keeps Yavimaya Granger and echo does not trigger again")
    void payingEchoKeepsYavimayaGrangerAndIsOneShot() {
        castAndDeclineLandSearch();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Yavimaya Granger");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Yavimaya Granger");
    }

    @Test
    @DisplayName("Echo waits for the controller upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndDeclineLandSearch();

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Yavimaya Granger");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Entering creates only the land-search trigger, not an echo-registration trigger")
    void enteringCreatesOnlyTheLandSearchTrigger() {
        harness.enterBattlefieldAndReturn(player1, new YavimayaGranger());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Yavimaya Granger");
    }

    @Test
    @DisplayName("A paid echo cost is deducted and the following upkeep has no echo ability to resolve")
    void paidEchoDoesNotAskForPaymentAtFollowingUpkeep() {
        castAndDeclineLandSearch();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Yavimaya Granger");
    }

    @Test
    @DisplayName("The basic-land search may fail to find even when a basic land is available")
    void mayFailToFindAnAvailableBasicLand() {
        setupAndCast();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Yavimaya Granger");
    }

    @Test
    @DisplayName("Accepting the search with no basic lands completes without finding a card")
    void searchWithNoBasicLandsCompletes() {
        setupAndCast();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes normally")
    void searchWithEmptyLibraryCompletes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Yavimaya Granger");
    }

    @Test
    @DisplayName("An opponent's Granger searches its controller's library")
    void opponentGrangerSearchesItsControllersLibrary() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setLibrary(player2, List.of(forest));
        harness.enterBattlefieldAndReturn(player2, new YavimayaGranger());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new YavimayaGranger(), "{2}{G}");
    }

    private void castAndDeclineLandSearch() {
        setupAndCast();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
    }

    private void setupLibraryWithBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
