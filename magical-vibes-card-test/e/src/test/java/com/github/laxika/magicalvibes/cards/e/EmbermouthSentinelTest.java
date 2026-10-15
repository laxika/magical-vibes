package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StormscaleScion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbermouthSentinel.class, Plains.class, StormscaleScion.class})
class EmbermouthSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Dragon, the accepted ETB search puts a basic land on top")
    void searchesBasicLandToTopWithoutDragon() {
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));

        acceptEtbSearch();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards()).containsExactly(basicLand);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With a Dragon, the accepted ETB search puts a basic land onto the battlefield tapped")
    void searchesBasicLandToBattlefieldTappedWithDragon() {
        harness.addToBattlefield(player1, new StormscaleScion());
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));

        acceptEtbSearch();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, basicLand.getName());
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search does nothing")
    void declinesEtbSearch() {
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsDragonDoesNotUpgradeSearch() {
        harness.addToBattlefield(player2, new StormscaleScion());
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));
        acceptEtbSearch();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(countPermanents(player1, basicLand.getName())).isZero();
    }

    @Test
    void dragonGainedAfterTriggeringUpgradesSearch() {
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new StormscaleScion());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, basicLand.getName()).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(basicLand);
    }

    @Test
    void dragonLostAfterTriggeringDoesNotUpgradeSearch() {
        harness.addToBattlefield(player1, new StormscaleScion());
        Permanent dragon = findPermanent(player1, "Stormscale Scion");
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(countPermanents(player1, basicLand.getName())).isZero();
    }

    @Test
    void mayFailToFindEvenWhenBasicLandIsAvailable() {
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));
        acceptEtbSearch();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(countPermanents(player1, basicLand.getName())).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void searchDoesNotFindNonbasicCards() {
        Card nonland = new EmbermouthSentinel();
        castSentinel(List.of(nonland));
        acceptEtbSearch();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void acceptedSearchOfEmptyLibraryCompletes() {
        castSentinel(List.of());
        acceptEtbSearch();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void castSentinel(List<Card> library) {
        harness.setHand(player1, List.of(new EmbermouthSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.castCreature(player1, 0);
    }

    private void acceptEtbSearch() {
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
    }
}
