package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SparasHeadquarters;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrokersHideout.class, CivilServant.class, Forest.class, Island.class, Mountain.class,
        Plains.class, SparasHeadquarters.class, Swamp.class})
class BrokersHideoutTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Brokers Hideout before the search trigger resolves")
    void enteringSacrificesIt() {
        BrokersHideout hideout = new BrokersHideout();
        harness.setHand(player1, List.of(hideout));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(hideout.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(hideout.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(hideout.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Forest, Plains, or Island and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playHideout();
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        setLibrary(plains, forest, island, new CivilServant());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains, forest, island);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playHideout();
        Card plains = new Plains();
        setLibrary(plains, new Forest(), new Island());

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(plains.getId()))
                .findFirst().orElseThrow();
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playHideout();
        setLibrary(new CivilServant());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Sacrifice and search resolve separately, with life gained only after the search")
    void reflexiveSearchWaitsForAnotherPriorityRound() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card forest = new Forest();
        setLibrary(forest);
        playHideout();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brokers Hideout");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Basic Mountains and Swamps and nonbasic lands with allowed types cannot be found")
    void rejectsDisallowedBasicTypesAndNonbasicTypedLands() {
        Card forest = new Forest();
        setLibrary(new Mountain(), new Swamp(), new SparasHeadquarters(), forest);
        playHideout();

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
    }

    @Test
    @DisplayName("Controller can fail to find an eligible land and still gains life")
    void failingToFindStillGainsLife() {
        harness.setLife(player1, 20);
        Card plains = new Plains();
        setLibrary(plains);
        playHideout();
        resolveToSearchPrompt();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An empty library does not prevent gaining life")
    void emptyLibraryStillGainsLife() {
        harness.setLife(player1, 20);
        setLibrary();
        playHideout();

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Brokers Hideout");
    }

    @Test
    @DisplayName("A Hideout that left the battlefield before its trigger resolves causes no search or life gain")
    void absentSourceCannotCreateReflexiveTrigger() {
        harness.setLife(player1, 20);
        Card plains = new Plains();
        setLibrary(plains);
        BrokersHideout hideout = new BrokersHideout();
        harness.setHand(player1, List.of(hideout));
        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getId().equals(hideout.getId()));
        harness.setHand(player1, List.of(hideout));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Brokers Hideout");
        harness.assertNotInGraveyard(player1, "Brokers Hideout");
    }

    private void playHideout() {
        harness.setHand(player1, List.of(new BrokersHideout()));
        harness.playLand(player1, 0);
    }

    private void resolveToSearchPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
