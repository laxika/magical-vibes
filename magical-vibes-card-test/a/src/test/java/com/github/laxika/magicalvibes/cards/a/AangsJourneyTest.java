package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HondenOfLifesWeb;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.SouthernAirTemple;
import com.github.laxika.magicalvibes.cards.w.WanShiTongLibrarian;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangsJourney.class, Forest.class, GrizzlyBears.class, HondenOfLifesWeb.class,
        SouthernAirTemple.class, WanShiTongLibrarian.class, PsychogenicProbe.class})
class AangsJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, searches for a basic land and gains 2 life")
    void withoutKickerSearchesForBasicLandAndGainsLife() {
        Forest forest = new Forest();
        cast(false, forest, new HondenOfLifesWeb());

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("When kicked, searches for a basic land and then a Shrine, then gains 2 life")
    void kickedSearchesForBasicLandAndShrineAndGainsLife() {
        Forest forest = new Forest();
        HondenOfLifesWeb shrine = new HondenOfLifesWeb();
        cast(true, forest, shrine);

        PendingInteraction.LibrarySearch basicLandSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(basicLandSearch).isNotNull();
        assertThat(basicLandSearch.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch shrineSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(shrineSearch).isNotNull();
        assertThat(shrineSearch.params().cards()).containsExactly(shrine);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, shrine);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A kicked Journey searches the library only once")
    void kickedSpellTriggersOpponentSearchAbilityOnce() {
        harness.addToBattlefield(player2, new WanShiTongLibrarian());
        cast(true, List.of(new Forest(), new SouthernAirTemple()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A kicked Journey shuffles only once after finding both cards")
    void kickedSpellTriggersShuffleAbilityOnce() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        cast(true, List.of(new Forest(), new SouthernAirTemple()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A kicked Journey can fail to find a basic land and still find a Shrine")
    void canDeclineLandAndFindShrine() {
        Forest forest = new Forest();
        SouthernAirTemple shrine = new SouthernAirTemple();
        cast(true, List.of(forest, shrine));

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shrine);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Aang's Journey");
    }

    @Test
    @DisplayName("A kicked Journey can find a basic land and decline the Shrine")
    void canFindLandAndDeclineShrine() {
        Forest forest = new Forest();
        SouthernAirTemple shrine = new SouthernAirTemple();
        cast(true, List.of(forest, shrine));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shrine);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A kicked Journey still gains life when the library is empty")
    void emptyLibraryStillGainsLife() {
        cast(true, List.of());

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Aang's Journey");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A kicked Journey finds a Shrine when there is no basic land")
    void findsShrineWithoutBasicLand() {
        SouthernAirTemple shrine = new SouthernAirTemple();
        cast(true, List.of(shrine));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shrine);
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A kicked Journey finds a basic land when there is no Shrine")
    void findsBasicLandWithoutShrine() {
        Forest forest = new Forest();
        cast(true, List.of(forest));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A kicked Journey may find neither card and still gain life")
    void canDeclineBothCards() {
        Forest forest = new Forest();
        SouthernAirTemple shrine = new SouthernAirTemple();
        cast(true, List.of(forest, shrine));

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shrine);
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without kicker, a library containing only a Shrine yields no card but gains life")
    void unkickedSpellCannotFindShrine() {
        SouthernAirTemple shrine = new SouthernAirTemple();
        cast(false, List.of(shrine));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shrine);
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(boolean kicked, Forest forest, HondenOfLifesWeb shrine) {
        cast(kicked, List.of(forest, shrine, new GrizzlyBears()));
    }

    private void cast(boolean kicked, List<Card> library) {
        harness.setHand(player1, List.of(new AangsJourney()));
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 4 : 2);
        harness.setLibrary(player1, library);

        if (kicked) {
            harness.castKickedSorcery(player1, 0, null);
        } else {
            harness.castAndResolveSorcery(player1, 0, 0);
            return;
        }
        harness.passBothPriorities();
    }
}
