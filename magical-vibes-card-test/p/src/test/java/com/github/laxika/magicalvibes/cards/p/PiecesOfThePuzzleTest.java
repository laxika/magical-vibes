package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiecesOfThePuzzle.class, Forest.class, DevilthornFox.class, JustTheWind.class})
class PiecesOfThePuzzleTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two instant and sorcery cards among the revealed five")
    void offersUpToTwoInstantsAndSorceries() {
        Card instant = new JustTheWind();
        Card forest = new Forest();
        Card fox = new DevilthornFox();
        Card instant2 = new JustTheWind();
        Card forest2 = new Forest();
        setTopCards(instant, forest, fox, instant2, forest2);

        castPiecesOfThePuzzle();

        GameData data = harness.getGameData();
        assertThat(data.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).maxCount())
                .isEqualTo(2);
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactlyInAnyOrder(instant.getId(), instant2.getId());
    }

    @Test
    @DisplayName("Puts two chosen spells into hand and the rest into the graveyard")
    void choosesTwoSpellsAndBinsTheRest() {
        Card instant = new JustTheWind();
        Card forest = new Forest();
        Card fox = new DevilthornFox();
        Card instant2 = new JustTheWind();
        Card forest2 = new Forest();
        setTopCards(instant, forest, fox, instant2, forest2);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), instant2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(instant, instant2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, fox, forest2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Allows keeping only one eligible spell")
    void choosesOnlyOneSpell() {
        Card instant = new JustTheWind();
        Card forest = new Forest();
        Card fox = new DevilthornFox();
        Card forest2 = new Forest();
        Card fox2 = new DevilthornFox();
        setTopCards(instant, forest, fox, forest2, fox2);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, fox, forest2, fox2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Puts all non-spells into the graveyard when no instant or sorcery is revealed")
    void noEligibleCardsGoToGraveyard() {
        Card forest = new Forest();
        Card fox = new DevilthornFox();
        Card forest2 = new Forest();
        Card fox2 = new DevilthornFox();
        Card forest3 = new Forest();
        setTopCards(forest, fox, forest2, fox2, forest3);

        castPiecesOfThePuzzle();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(forest, fox, forest2, fox2, forest3);
    }

    @Test
    @DisplayName("Can keep an instant and a sorcery while leaving the sixth card untouched")
    void keepsInstantAndSorceryFromOnlyTopFive() {
        Card instant = new JustTheWind();
        Card sorcery = new PiecesOfThePuzzle();
        Card unchosenSpell = new JustTheWind();
        Card forest = new Forest();
        Card fox = new DevilthornFox();
        Card sixth = new PiecesOfThePuzzle();
        setTopCards(instant, sorcery, unchosenSpell, forest, fox, sixth);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(instant, sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosenSpell, forest, fox);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can keep two sorceries")
    void keepsTwoSorceries() {
        Card first = new PiecesOfThePuzzle();
        Card second = new PiecesOfThePuzzle();
        Card forest = new Forest();
        setTopCards(first, second, forest);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can decline all eligible cards in a short library")
    void choosesZeroEligibleCards() {
        Card instant = new JustTheWind();
        Card sorcery = new PiecesOfThePuzzle();
        setTopCards(instant, sorcery);

        castPiecesOfThePuzzle();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves without a choice when the library is empty")
    void emptyLibrary() {
        setTopCards();

        castPiecesOfThePuzzle();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castPiecesOfThePuzzle() {
        harness.setHand(player1, List.of(new PiecesOfThePuzzle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setTopCards(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
