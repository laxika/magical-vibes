package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JungleHollow;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MigratoryGreathorn.class, Forest.class, GrizzlyBears.class, JungleHollow.class})
class MigratoryGreathornTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating searches for a basic land and puts it onto the battlefield tapped")
    void mutatingSearchesForBasicLandToBattlefieldTapped() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), forest));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, greathorn, List.of(greathorn.getCard()), player1.getId()));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mutating another creature does not trigger Migratory Greathorn")
    void anotherCreatureMutatingDoesNotTrigger() {
        addCreatureReady(player1, new MigratoryGreathorn());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, bear, List.of(bear.getCard()), player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Casting normally does not search for a land")
    void normalCastDoesNotSearch() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new MigratoryGreathorn(), "{3}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Migratory Greathorn");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Migratory Greathorn can be cast for its mutate cost")
    void canCastForMutateCost() {
        Permanent target = addCreatureReady(player1, new MigratoryGreathorn());
        harness.setHand(player1, List.of(new MigratoryGreathorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A restricted search may fail to find even when a basic land is available")
    void mayFailToFindAnAvailableLand() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, greathorn, List.of(greathorn.getCard()), player1.getId()));
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(greathorn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("A library without basic lands is searched and shuffled without putting anything onto the battlefield")
    void noBasicLandStillShuffles() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        MigratoryGreathorn other = new MigratoryGreathorn();
        harness.setLibrary(player1, List.of(other));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, greathorn, List.of(greathorn.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(greathorn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Each mutation searches for only one land from the controller's library")
    void repeatedMutationsSearchControllerLibraryOneLandAtATime() {
        Permanent greathorn = addCreatureReady(player2, new MigratoryGreathorn());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest opponentsLand = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(opponentsLand));

        for (int mutation = 0; mutation < 2; mutation++) {
            harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, greathorn, List.of(greathorn.getCard()), player2.getId()));
            resolveAllTriggers();
            harness.handleCardChosen(player2, 0);

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(1 - mutation);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsLand);
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2 + mutation);
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
        }

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest)
                .allMatch(Permanent::isTapped)
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("A mutation trigger still searches after its source leaves the battlefield")
    void searchResolvesWithoutSource() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkMutateTriggers(
                    gd, greathorn, List.of(greathorn.getCard()), player1.getId());
            gd.playerBattlefields.get(player1.getId()).remove(greathorn);
            gd.playerGraveyards.get(player1.getId()).add(greathorn.getCard());
        });
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search excludes nonbasic lands")
    void nonbasicLandIsNotEligible() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        JungleHollow nonbasic = new JungleHollow();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(nonbasic, forest));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, greathorn, List.of(greathorn.getCard()), player1.getId()));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library is searched and shuffled without waiting for a choice")
    void emptyLibraryDoesNotWaitForInput() {
        Permanent greathorn = addCreatureReady(player1, new MigratoryGreathorn());
        harness.setLibrary(player1, List.of());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, greathorn, List.of(greathorn.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(greathorn);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }
}
