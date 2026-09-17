package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecruiterOfTheGuard.class, Ornithopter.class, GrizzlyBears.class, GiantSpider.class})
class RecruiterOfTheGuardTest extends BaseCardTest {

    @BeforeAll
    static void registerTestOracle() {
        Card.registerOracle("RecruiterOfTheGuard", new OracleData(
                "Recruiter of the Guard", CardType.CREATURE, java.util.Set.of(), "{2}{W}",
                CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardColor.WHITE), java.util.Set.of(),
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                "When Recruiter of the Guard enters the battlefield, you may search your library for a creature card with toughness 2 or less, reveal it, put it into your hand, then shuffle.",
                1, 1, java.util.Set.of(), null, null, null));
    }

    @Test
    @DisplayName("ETB offers creature cards with toughness 2 or less")
    void etbOffersLowToughnessCreatures() {
        setupAndCast();
        List<Card> library = List.of(new Ornithopter(), new GrizzlyBears(), new GiantSpider());
        harness.setLibrary(player1, library);

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand")
    void choosingCreaturePutsItIntoHand() {
        setupAndCast();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new GiantSpider()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningSearchLeavesLibraryUntouched() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GiantSpider()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new RecruiterOfTheGuard(), "{2}{W}");
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
