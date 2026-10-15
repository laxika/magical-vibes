package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SaguArcher;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BitterRevelation.class, WetlandSambar.class, SaguArcher.class, Island.class, Plains.class})
class BitterRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two of the top four cards into hand, the rest into the graveyard, and loses 2 life")
    void choosesTwoCardsAndLosesLife() {
        Card top1 = new WetlandSambar();
        Card top2 = new SaguArcher();
        Card top3 = new Island();
        Card top4 = new Plains();
        GameData data = harness.getGameData();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4));

        harness.setHand(player1, List.of(new BitterRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId(), top3.getId()));

        assertThat(data.playerHands.get(player1.getId())).containsExactly(top1, top3);
        assertThat(data.playerDecks.get(player1.getId())).isEmpty();
        assertThat(data.playerGraveyards.get(player1.getId())).contains(top2, top4);
        assertThat(data.getLife(player1.getId())).isEqualTo(18);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void putsAllAvailableCardsIntoHandWhenLibraryHasAtMostTwoCards(int librarySize) {
        List<Card> cards = List.<Card>of(new Island(), new Plains()).subList(0, librarySize);
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new BitterRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).first().isInstanceOf(BitterRevelation.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4})
    void requiresExactlyTwoCardsWhenAtLeastThreeAreAvailable(int librarySize) {
        List<Card> cards = List.<Card>of(new WetlandSambar(), new SaguArcher(), new Island(), new Plains())
                .subList(0, librarySize);
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new BitterRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(cards.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(cards.get(0).getId(), cards.get(1).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cards.get(0), cards.get(1));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(cards.subList(2, librarySize));
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void leavesCardsBelowTheTopFourInLibrary() {
        Card top1 = new WetlandSambar();
        Card top2 = new SaguArcher();
        Card top3 = new Island();
        Card top4 = new Plains();
        Card fifth = new Island();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4, fifth));
        harness.setHand(player1, List.of(new BitterRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId(), top4.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top2, top4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top1, top3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
