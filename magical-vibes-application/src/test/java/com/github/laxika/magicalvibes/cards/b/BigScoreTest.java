package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BigScore.class, Forest.class, GrizzlyBears.class})
class BigScoreTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card, draws two cards, and creates two Treasures")
    void discardsDrawsAndCreatesTreasures() {
        BigScore spell = new BigScore();
        Forest discarded = new Forest();
        GrizzlyBears firstDraw = new GrizzlyBears();
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
}
