package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RaffinesInformant;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.x.XandersLounge;
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

@CardUsed({MaestrosTheater.class, RaffinesInformant.class, Island.class, Mountain.class, Plains.class, Swamp.class, XandersLounge.class})
class MaestrosTheaterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Maestros Theater before the search trigger resolves")
    void enteringSacrificesIt() {
        MaestrosTheater theater = new MaestrosTheater();
        harness.setHand(player1, List.of(theater));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(theater.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(theater.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(theater.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Island, Swamp, or Mountain and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playTheater();
        Card island = new Island();
        Card swamp = new Swamp();
        Card mountain = new Mountain();
        setLibrary(island, swamp, mountain, new RaffinesInformant(), new Plains(), new XandersLounge());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(island, swamp, mountain);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playTheater();
        Card island = new Island();
        setLibrary(island, new Swamp(), new Mountain());

        resolveToSearchPrompt();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = findPermanent(player1, "Island");
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(island).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playTheater();
        setLibrary(new RaffinesInformant(), new Plains());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Failing to find an available basic land still gains life")
    void mayFailToFind() {
        playTheater();
        Card island = new Island();
        setLibrary(island);

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, -1);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No search or life gain if Theater leaves before its entry trigger resolves")
    void sourceGoneBeforeSacrifice() {
        playTheater();
        Card island = new Island();
        setLibrary(island);
        Permanent theater = findPermanent(player1, "Maestros Theater");
        harness.getPermanentRemovalService().removePermanentToHand(gd, theater);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Maestros Theater");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Empty library still allows the reflexive trigger to gain life")
    void emptyLibraryStillGainsLife() {
        playTheater();
        setLibrary();

        resolveToSearchPrompt();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void playTheater() {
        harness.setHand(player1, List.of(new MaestrosTheater()));
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
