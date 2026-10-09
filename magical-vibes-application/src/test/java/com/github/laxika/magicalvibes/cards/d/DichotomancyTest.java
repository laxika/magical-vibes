package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dichotomancy.class, AvenMindcensor.class, GossamerPhantasm.class,
        DuneriderOutlaw.class, UrborgTombOfYawgmoth.class})
class DichotomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the target opponent's library for each tapped nonland permanent")
    void searchesForTappedNonlandPermanentNames() {
        Permanent tappedPhantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        tappedPhantasm.tap();
        Permanent tappedOutlaw = harness.addToBattlefieldAndReturn(player2, new DuneriderOutlaw());
        tappedOutlaw.tap();
        harness.addToBattlefield(player2, new DuneriderOutlaw());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        tappedLand.tap();

        harness.setLibrary(player2, List.of(new GossamerPhantasm(), new DuneriderOutlaw()));

        castAndResolveDichotomancy();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch).isNotNull();
        assertThat(firstSearch.params().playerId()).isEqualTo(player1.getId());
        assertThat(firstSearch.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(firstSearch.params().battlefieldControllerId()).isEqualTo(player1.getId());
        assertThat(firstSearch.params().cards()).extracting("name")
                .containsExactly("Gossamer Phantasm");

        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().cards()).extracting("name")
                .containsExactly("Dunerider Outlaw");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertOnBattlefield(player1, "Dunerider Outlaw");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Searches once for every tapped nonland permanent, including repeated names")
    void searchesOncePerTappedPermanentWhenNamesRepeat() {
        Permanent firstPhantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        firstPhantasm.tap();
        Permanent secondPhantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        secondPhantasm.tap();
        harness.setLibrary(player2, List.of(new GossamerPhantasm(), new GossamerPhantasm()));

        castAndResolveDichotomancy();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch).isNotNull();
        assertThat(firstSearch.params().cards()).hasSize(2);
        assertThat(firstSearch.params().cards()).extracting("name")
                .containsOnly("Gossamer Phantasm");
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().cards()).hasSize(1);
        assertThat(secondSearch.params().cards()).extracting("name")
                .containsExactly("Gossamer Phantasm");
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Gossamer Phantasm")).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only tapped nonlands create searches")
    void ignoresUntappedPermanentsAndLands() {
        harness.addToBattlefield(player2, new DuneriderOutlaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        land.tap();
        harness.setLibrary(player2, List.of(new DuneriderOutlaw()));

        castAndResolveDichotomancy();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Continues with later permanents when Aven Mindcensor hides the first name")
    void continuesAfterRestrictedSearchFindsNoTopMatch() {
        Permanent mindcensor = harness.addToBattlefieldAndReturn(player2, new AvenMindcensor());
        mindcensor.tap();
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        phantasm.tap();
        harness.setLibrary(player2, List.of(
                new GossamerPhantasm(), new GossamerPhantasm(), new GossamerPhantasm(),
                new GossamerPhantasm(), new AvenMindcensor()));

        castAndResolveDichotomancy();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name")
                .containsExactly("Gossamer Phantasm", "Gossamer Phantasm", "Gossamer Phantasm", "Gossamer Phantasm");
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Gossamer Phantasm")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new Dichotomancy()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Suspend exiles Dichotomancy with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        Dichotomancy card = suspendDichotomancy();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("Suspended Dichotomancy can be cast without paying its mana cost")
    void suspendedSpellCastsAfterLastCounterIsRemoved() {
        Permanent tappedPhantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        tappedPhantasm.tap();
        harness.setLibrary(player2, List.of(new GossamerPhantasm()));
        Dichotomancy card = suspendDichotomancy();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().battlefieldControllerId()).isEqualTo(player1.getId());
        assertThat(search.params().cards()).extracting("name")
                .containsExactly("Gossamer Phantasm");

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Gossamer Phantasm")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    private Dichotomancy suspendDichotomancy() {
        Dichotomancy card = new Dichotomancy();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    @Test
    @DisplayName("Keeps found cards off the battlefield until every search is finished")
    void foundCardsEnterOnlyAfterAllSearches() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        phantasm.tap();
        Permanent outlaw = harness.addToBattlefieldAndReturn(player2, new DuneriderOutlaw());
        outlaw.tap();
        harness.setLibrary(player2, List.of(new GossamerPhantasm(), new DuneriderOutlaw()));

        castAndResolveDichotomancy();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertOnBattlefield(player1, "Dunerider Outlaw");
    }

    @Test
    @DisplayName("Failing to find one name still allows finding the next name")
    void canDeclineOneSearchAndContinue() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        phantasm.tap();
        Permanent outlaw = harness.addToBattlefieldAndReturn(player2, new DuneriderOutlaw());
        outlaw.tap();
        harness.setLibrary(player2, List.of(new GossamerPhantasm(), new DuneriderOutlaw()));

        castAndResolveDichotomancy();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertOnBattlefield(player1, "Dunerider Outlaw");
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name")
                .containsExactly("Gossamer Phantasm");
    }

    @Test
    @DisplayName("Shuffles only once after all searches finish")
    void shufflesAfterAllSearchesRatherThanBetweenPicks() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        phantasm.tap();
        Permanent outlaw = harness.addToBattlefieldAndReturn(player2, new DuneriderOutlaw());
        outlaw.tap();
        harness.setLibrary(player2, List.of(new GossamerPhantasm(), new DuneriderOutlaw()));

        castAndResolveDichotomancy();
        int logStart = gd.gameLog.size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.subList(logStart, gd.gameLog.size()))
                .noneMatch(entry -> entry.plainText().contains("library is shuffled"));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.subList(logStart, gd.gameLog.size()).stream()
                .filter(entry -> entry.plainText().contains("library is shuffled")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Dichotomancy in exile without time counters")
    void canDeclineSuspendedSpell() {
        Dichotomancy card = suspendDichotomancy();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveDichotomancy() {
        harness.setHand(player1, List.of(new Dichotomancy()));
        harness.addMana(player1, ManaColor.BLUE, 9);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
