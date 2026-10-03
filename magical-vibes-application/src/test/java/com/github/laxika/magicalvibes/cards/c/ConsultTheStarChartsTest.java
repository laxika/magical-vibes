package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsultTheStarCharts.class, Forest.class, GrizzlyBears.class, Shock.class})
class ConsultTheStarChartsTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at one card per land and puts one into hand")
    void looksAtOneCardPerLandWithoutKicker() {
        Card chosen = new GrizzlyBears();
        Card bottomed = new Shock();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(chosen, bottomed));

        harness.castFromHand(player1, new ConsultTheStarCharts(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                .maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed);
    }

    @Test
    @DisplayName("With kicker, keeps two cards per the land count")
    void keepsTwoCardsWhenKicked() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        Card fourth = new Shock();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new ConsultTheStarCharts()));
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        addKickedMana();

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                .maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(third, fourth);
    }

    @Test
    void noLandsLeavesLibraryUnchanged() {
        Card top = new Forest();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(top));

        harness.castFromHand(player1, new ConsultTheStarCharts(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesLandCountAtResolutionAndBottomsOnlyLookedAtCards() {
        Card first = new Forest();
        Card second = new ConsultTheStarCharts();
        Card untouched = new Forest();
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.castFromHand(player1, new ConsultTheStarCharts(), "{1}{U}");
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.handleMultipleCardsChosen(player1, List.of(untouched.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedSpellWithOnlyOneAvailableCardPutsItIntoHand() {
        Card onlyCard = new Forest();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new ConsultTheStarCharts()));
        addKickedMana();

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryResolvesWithoutDrawing() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new ConsultTheStarCharts(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
