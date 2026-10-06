package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.e.EriettesTemptingApple;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GadwicksFirstDuel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RowansGrimSearch.class, DarksteelRelic.class, GrizzlyBears.class, Shock.class,
        EriettesTemptingApple.class, GadwicksFirstDuel.class})
class RowansGrimSearchTest extends BaseCardTest {

    @Test
    void drawsTwoAndLosesTwoLifeWithoutBargain() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Shock();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

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

    @Test
    @CardUsed({RowansGrimSearch.class, EriettesTemptingApple.class})
    void bargainMayPutNoCardsOnTopAndDrawsFromBelowTheLookedAtCards() {
        List<Card> library = List.of(new RowansGrimSearch(), new RowansGrimSearch(),
                new RowansGrimSearch(), new RowansGrimSearch(),
                new RowansGrimSearch(), new RowansGrimSearch());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        harness.addToBattlefield(player1, new EriettesTemptingApple());
        addMana();

        harness.castKickedInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Eriette's Tempting Apple"));
        harness.assertInGraveyard(player1, "Eriette's Tempting Apple");
        harness.assertNotOnBattlefield(player1, "Eriette's Tempting Apple");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(4), library.get(5));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsAll(library.subList(0, 4));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({RowansGrimSearch.class, EriettesTemptingApple.class})
    void bargainLooksAtOnlyTheAvailableCardsAndCanReverseTheirOrder() {
        Card first = new RowansGrimSearch();
        Card second = new RowansGrimSearch();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        harness.addToBattlefield(player1, new EriettesTemptingApple());
        addMana();

        harness.castKickedInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Eriette's Tempting Apple"));
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(first, second);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({RowansGrimSearch.class, GadwicksFirstDuel.class})
    void bargainCanSacrificeAnEnchantment() {
        Card first = new RowansGrimSearch();
        Card second = new RowansGrimSearch();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        harness.addToBattlefield(player1, new GadwicksFirstDuel());
        addMana();

        harness.castKickedInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Gadwick's First Duel"));
        harness.assertInGraveyard(player1, "Gadwick's First Duel");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({RowansGrimSearch.class, GrizzlyBears.class})
    void bargainCannotSacrificeANontokenCreatureThatIsNotAnArtifactOrEnchantment() {
        harness.setHand(player1, List.of(new RowansGrimSearch()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Rowan's Grim Search");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
