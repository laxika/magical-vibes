package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RowansGrimSearch.class, DarksteelRelic.class, GrizzlyBears.class, Shock.class})
class RowansGrimSearchTest extends BaseCardTest {

    @Test
    void drawsTwoAndLosesTwoLifeWithoutBargain() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Shock();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Rowan's Grim Search");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainPutsUpToTwoCardsBackOnTopThenDrawsThemAndGraveyardsTheRest() {
        List<Card> library = List.of(
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        harness.addToBattlefield(player1, new DarksteelRelic());
        addMana();

        harness.castKickedInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Darksteel Relic"));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactlyElementsOf(library);
        harness.handleCardChosen(player1, 1);

        PendingInteraction.LibrarySearch secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondChoice.params().cards())
                .containsExactly(library.get(0), library.get(2), library.get(3));
        assertThat(secondChoice.params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).contains(library.get(1), library.get(3));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(library.get(0), library.get(2));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Rowan's Grim Search");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainMayPutOnlyOneCardOnTop() {
        List<Card> library = List.of(
                new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        harness.addToBattlefield(player1, new DarksteelRelic());
        addMana();

        harness.castKickedInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Darksteel Relic"));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(library.get(0), library.get(4));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(library.get(1), library.get(2), library.get(3));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
