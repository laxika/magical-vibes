package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlurryVisionary.class, GrizzlyBears.class, LlanowarElves.class})
class BlurryVisionaryTest extends BaseCardTest {

    @Test
    void combinesTheTopTwoCardsWithTheChosenCardInFront() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, second));
        harness.castFromHand(player1, new BlurryVisionary(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        Card combined = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(second.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(combined.isModalDoubleFaced()).isTrue();
        assertThat(combined.getBackFaceCard().getId()).isEqualTo(top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void putsTheOnlyTopCardIntoHandWithoutMakingAnMdfc() {
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new BlurryVisionary(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(onlyCard.isModalDoubleFaced()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNothingWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new BlurryVisionary(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Blurry Visionary");
    }

    @Test
    void choosingTheTopCardPreservesTheRestOfTheLibraryInOrder() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        Card fourth = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, second, third, fourth));

        harness.castFromHand(player1, new BlurryVisionary(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card combined = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(combined.getId()).isEqualTo(top.getId());
        assertThat(combined.getBackFaceCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void canCastEitherFaceOfTheCombinedCard(int face) {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.castFromHand(player1, new BlurryVisionary(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        if (face == 0) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }
        harness.castCreature(player1, 0, face);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, face == 0 ? "Grizzly Bears" : "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, face == 0 ? "Llanowar Elves" : "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void lookingAtTheTopCardsDoesNotRevealThemInThePublicLog() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves()));

        harness.castFromHand(player1, new BlurryVisionary(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).noneMatch(entry ->
                entry.plainText().contains("Grizzly Bears")
                        || entry.plainText().contains("Llanowar Elves"));
    }
}
