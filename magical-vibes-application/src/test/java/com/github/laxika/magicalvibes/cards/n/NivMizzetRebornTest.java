package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.cards.d.Deathsprout;
import com.github.laxika.magicalvibes.cards.d.DovinsVeto;
import com.github.laxika.magicalvibes.cards.t.TeferisTimeTwist;
import com.github.laxika.magicalvibes.cards.u.UginTheIneffable;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivMizzetReborn.class, DovinsVeto.class, Deathsprout.class,
        TeferisTimeTwist.class, UginTheIneffable.class, NicolBolasDragonGod.class})
class NivMizzetRebornTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses one card for each represented color pair")
    void choosesOneCardForEachRepresentedPair() {
        Card azorius = card("Azorius card", CardColor.WHITE, CardColor.BLUE);
        Card secondAzorius = card("Second Azorius card", CardColor.WHITE, CardColor.BLUE);
        Card golgari = card("Golgari card", CardColor.BLACK, CardColor.GREEN);
        Card monocolored = card("Monocolored card", CardColor.RED);
        Card threeColor = card("Three-color card", CardColor.WHITE, CardColor.BLUE, CardColor.BLACK);
        setLibrary(azorius, secondAzorius, golgari, monocolored, threeColor);

        castAndResolve();

        PendingInteraction.NivMizzetColorPairChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.NivMizzetColorPairChoice.class);
        assertThat(choice.requiredPairCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(azorius.getId(), secondAzorius.getId(), golgari.getId());

        answer(List.of(secondAzorius.getId(), golgari.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(secondAzorius, golgari);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(azorius, monocolored, threeColor);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(azorius, monocolored, threeColor);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Rejects choosing two cards from the same color pair")
    void rejectsDuplicateColorPair() {
        Card azorius = card("Azorius card", CardColor.WHITE, CardColor.BLUE);
        Card secondAzorius = card("Second Azorius card", CardColor.WHITE, CardColor.BLUE);
        Card golgari = card("Golgari card", CardColor.BLACK, CardColor.GREEN);
        setLibrary(azorius, secondAzorius, golgari);

        castAndResolve();

        assertThatThrownBy(() -> answer(List.of(azorius.getId(), secondAzorius.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.NivMizzetColorPairChoice.class);

        answer(List.of(azorius.getId(), golgari.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(azorius, golgari);
    }

    @Test
    @DisplayName("Only the top ten exactly-two-color cards are eligible")
    void onlyTopTenExactPairsAreEligible() {
        Card azorius = card("Azorius card", CardColor.WHITE, CardColor.BLUE);
        Card monoWhite = card("White card", CardColor.WHITE);
        Card colorless = card("Colorless card");
        Card threeColor = card("Three-color card", CardColor.WHITE, CardColor.BLUE, CardColor.BLACK);
        Card filler1 = card("Filler 1", CardColor.RED);
        Card filler2 = card("Filler 2", CardColor.GREEN);
        Card filler3 = card("Filler 3", CardColor.BLACK);
        Card filler4 = card("Filler 4", CardColor.BLUE);
        Card filler5 = card("Filler 5", CardColor.WHITE);
        Card filler6 = card("Filler 6", CardColor.RED);
        Card deepPair = card("Deep pair", CardColor.BLACK, CardColor.RED);
        setLibrary(azorius, monoWhite, colorless, threeColor, filler1, filler2, filler3, filler4, filler5, filler6,
                deepPair);

        castAndResolve();

        PendingInteraction.NivMizzetColorPairChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.NivMizzetColorPairChoice.class);
        assertThat(choice.revealedCards()).containsExactly(
                azorius, monoWhite, colorless, threeColor, filler1, filler2, filler3, filler4, filler5, filler6);
        assertThat(choice.validCardIds()).containsExactly(azorius.getId());

        answer(List.of(azorius.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(azorius);
        assertThat(gd.playerDecks.get(player1.getId())).contains(deepPair);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
    }

    @Test
    @DisplayName("With no represented color pair, all revealed cards go to the library bottom")
    void noRepresentedPairNeedsNoChoice() {
        Card monoWhite = card("White card", CardColor.WHITE);
        Card monoRed = card("Red card", CardColor.RED);
        Card colorless = card("Colorless card");
        setLibrary(monoWhite, monoRed, colorless);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(monoWhite, monoRed, colorless);
    }

    @Test
    @DisplayName("An empty library resolves without requiring a choice")
    void emptyLibraryNeedsNoChoice() {
        setLibrary();

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Niv-Mizzet Reborn");
    }

    @Test
    @DisplayName("Every represented pair must be selected and other colors cannot be selected")
    void rejectsMissingPairsAndIneligibleCards() {
        Card azorius = new DovinsVeto();
        Card golgari = new Deathsprout();
        Card monocolored = new TeferisTimeTwist();
        Card colorless = new UginTheIneffable();
        Card threeColor = new NicolBolasDragonGod();
        Card fiveColor = new NivMizzetReborn();
        setLibrary(azorius, golgari, monocolored, colorless, threeColor, fiveColor);

        castAndResolve();

        assertThatThrownBy(() -> answer(List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> answer(List.of(azorius.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> answer(List.of(azorius.getId(), azorius.getId())))
                .isInstanceOf(IllegalStateException.class);
        for (Card ineligible : List.of(monocolored, colorless, threeColor, fiveColor)) {
            assertThatThrownBy(() -> answer(List.of(azorius.getId(), ineligible.getId())))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        answer(List.of(azorius.getId(), golgari.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(azorius, golgari);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(monocolored, colorless, threeColor, fiveColor);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unrevealed cards stay above the unchosen revealed cards")
    void preservesUnrevealedLibraryOrder() {
        Card azorius = new DovinsVeto();
        List<Card> unchosen = java.util.stream.IntStream.range(0, 9)
                .mapToObj(i -> (Card) new TeferisTimeTwist()).toList();
        Card eleventh = new Deathsprout();
        Card twelfth = new UginTheIneffable();
        List<Card> library = new java.util.ArrayList<>();
        library.add(azorius);
        library.addAll(unchosen);
        library.add(eleventh);
        library.add(twelfth);
        harness.setLibrary(player1, library);

        castAndResolve();
        answer(List.of(azorius.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(azorius);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(11)
                .startsWith(eleventh, twelfth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 11))
                .containsExactlyInAnyOrderElementsOf(unchosen);
    }

    private static Card card(String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setColors(List.of(colors));
        return card;
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new NivMizzetReborn(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void answer(List<java.util.UUID> cardIds) {
        harness.handleMultipleCardsChosen(player1, cardIds);
    }
}
