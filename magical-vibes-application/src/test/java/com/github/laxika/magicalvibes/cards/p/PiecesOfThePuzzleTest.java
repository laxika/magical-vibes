package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PiecesOfThePuzzleTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two instant and sorcery cards among the revealed five")
    void offersUpToTwoInstantsAndSorceries() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock2 = new Shock();
        Card forest2 = new Forest();
        setTopCards(shock, forest, bears, shock2, forest2);

        castPiecesOfThePuzzle();

        GameData data = harness.getGameData();
        assertThat(data.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).maxCount())
                .isEqualTo(2);
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactlyInAnyOrder(shock.getId(), shock2.getId());
    }

    @Test
    @DisplayName("Puts two chosen spells into hand and the rest into the graveyard")
    void choosesTwoSpellsAndBinsTheRest() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock2 = new Shock();
        Card forest2 = new Forest();
        setTopCards(shock, forest, bears, shock2, forest2);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), shock2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(shock, shock2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, bears, forest2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows keeping only one eligible spell")
    void choosesOnlyOneSpell() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card forest2 = new Forest();
        Card bears2 = new GrizzlyBears();
        setTopCards(shock, forest, bears, forest2, bears2);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, bears, forest2, bears2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts all non-spells into the graveyard when no instant or sorcery is revealed")
    void noEligibleCardsGoToGraveyard() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card forest2 = new Forest();
        Card bears2 = new GrizzlyBears();
        Card forest3 = new Forest();
        setTopCards(forest, bears, forest2, bears2, forest3);

        castPiecesOfThePuzzle();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(forest, bears, forest2, bears2, forest3);
    }

    private void castPiecesOfThePuzzle() {
        harness.setHand(player1, List.of(new PiecesOfThePuzzle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setTopCards(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
