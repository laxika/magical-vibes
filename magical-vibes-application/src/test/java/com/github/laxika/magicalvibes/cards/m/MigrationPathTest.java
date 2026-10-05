package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.cards.r.RuggedHighlands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MigrationPath.class, Plains.class, Forest.class, AlmightyBrushwagg.class, RuggedHighlands.class,
        PastInFlames.class})
class MigrationPathTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two basic lands and puts them onto the battlefield tapped")
    void searchesForBasicLandsTapped() {
        resolveMigrationPath(List.of(new Plains(), new Forest(), new AlmightyBrushwagg()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).hasSize(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("Cycling discards Migration Path and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MigrationPath()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Migration Path");
        harness.assertInHand(player1, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Granted flashback still searches for up to two basic lands")
    void grantedFlashbackStillFindsTwoLands() {
        harness.setGraveyard(player1, List.of(new MigrationPath()));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFlashback(player1, 0, null);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Migration Path"));
    }

    @Test
    @DisplayName("Finds at most two lands even when more are available")
    void findsAtMostTwoLands() {
        resolveMigrationPath(List.of(new Forest(), new Forest(), new Plains()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(2).allMatch(permanent -> permanent.isTapped());
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("May choose zero lands even when basic lands are available")
    void mayChooseZeroLands() {
        resolveMigrationPath(List.of(new Plains(), new Forest()));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("May choose only one land when a second basic land is available")
    void mayChooseOneLand() {
        resolveMigrationPath(List.of(new Plains(), new Forest()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("Finishes after finding the only basic land and excludes nonbasic lands")
    void findsOnlyAvailableBasicLand() {
        resolveMigrationPath(List.of(new Forest(), new RuggedHighlands(), new AlmightyBrushwagg()));

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Rugged Highlands");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("Resolves without finding a card when no basic lands exist")
    void noBasicLands() {
        resolveMigrationPath(List.of(new RuggedHighlands(), new AlmightyBrushwagg()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void emptyLibrary() {
        resolveMigrationPath(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Migration Path");
    }

    @Test
    @DisplayName("Cycling pays and discards immediately but draws only on resolution")
    void cyclingDiscardsAsCost() {
        harness.setHand(player1, List.of(new MigrationPath()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Migration Path");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveMigrationPath(List<Card> library) {
        harness.setHand(player1, List.of(new MigrationPath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, library);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
