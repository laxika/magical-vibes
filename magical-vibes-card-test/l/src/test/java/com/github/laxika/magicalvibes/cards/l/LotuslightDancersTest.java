package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FelotharDawnOfTheAbzan;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lotuslight Dancers")
@CardUsed({LotuslightDancers.class, DarkRitual.class, FelotharDawnOfTheAbzan.class, GiantGrowth.class, Opt.class,
        ObNixilisUnshackled.class})
class LotuslightDancersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one card of each required color into the graveyard")
    void etbSearchesForBlackGreenAndBlueCards() {
        Card black = new DarkRitual();
        Card green = new GiantGrowth();
        Card blue = new Opt();
        Card multicolored = new FelotharDawnOfTheAbzan();
        castWithLibrary(List.of(black, green, blue, multicolored));

        resolveTrigger();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .hasSize(3)
                .contains(black.getId(), blue.getId())
                .containsAnyOf(green.getId(), multicolored.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .hasSize(1)
                .containsAnyOf(green.getId(), multicolored.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A multicolored card can be chosen for one required color")
    void multicoloredCardCanSatisfyColorSearch() {
        Card multicolored = new FelotharDawnOfTheAbzan();
        castWithLibrary(List.of(multicolored));

        resolveTrigger();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(multicolored.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each color search may fail to find a card")
    void searchesMayFindFewerThanThreeCards() {
        Card black = new DarkRitual();
        Card green = new GiantGrowth();
        Card blue = new Opt();
        castWithLibrary(List.of(black, green, blue));

        resolveTrigger();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(black.getId(), green.getId(), blue.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({LotuslightDancers.class})
    @DisplayName("Three distinct multicolored cards can fill all three color requirements")
    void threeMulticoloredCardsCanBeFound() {
        Card first = new LotuslightDancers();
        Card second = new LotuslightDancers();
        Card third = new LotuslightDancers();
        castWithLibrary(List.of(first, second, third));

        resolveTrigger();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({LotuslightDancers.class})
    @DisplayName("Found cards move to the graveyard only after all search choices are complete")
    void foundCardsWaitUntilSearchIsComplete() {
        Card first = new LotuslightDancers();
        Card second = new LotuslightDancers();
        Card third = new LotuslightDancers();
        castWithLibrary(List.of(first, second, third));

        resolveTrigger();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
    }

    @Test
    @CardUsed({LotuslightDancers.class, ObNixilisUnshackled.class})
    @DisplayName("Searching for three colors is one library search even when no cards are found")
    void abilityCausesOnlyOneSearchTrigger() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        castWithLibrary(List.of());

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ObNixilisUnshackled.class);
    }

    private void castWithLibrary(List<Card> library) {
        harness.castFromHand(player1, new LotuslightDancers(), "{2}{B}{G}{U}");
        harness.setLibrary(player1, library);
    }

    private void resolveTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
