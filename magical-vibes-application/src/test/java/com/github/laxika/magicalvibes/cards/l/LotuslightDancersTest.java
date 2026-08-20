package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FelotharDawnOfTheAbzan;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lotuslight Dancers")
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

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(black.getId(), green.getId(), blue.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(multicolored.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A multicolored card can be chosen for one required color")
    void multicoloredCardCanSatisfyColorSearch() {
        Card multicolored = new FelotharDawnOfTheAbzan();
        castWithLibrary(List.of(multicolored));

        resolveTrigger();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(black.getId(), green.getId(), blue.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new LotuslightDancers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.setLibrary(player1, library);
    }

    private void resolveTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
