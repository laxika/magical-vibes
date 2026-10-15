package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BigScore.class, Forest.class})
class BigScoreTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card, draws two cards, and creates two Treasures")
    void discardsDrawsAndCreatesTreasures() {
        BigScore spell = new BigScore();
        Forest discarded = new Forest();
        BigScore firstDraw = new BigScore();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast without another card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new BigScore()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Discard is paid before resolution even when the discarded card precedes the spell")
    void paysDiscardBeforeResolution() {
        BigScore discarded = new BigScore();
        BigScore spell = new BigScore();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(discarded, spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 1, null, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discarded, spell);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The spell cannot discard itself when another card is available")
    void cannotDiscardItself() {
        BigScore spell = new BigScore();
        Forest otherCard = new Forest();
        harness.setHand(player1, List.of(spell, otherCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell, otherCard);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Forest");
    }
}
