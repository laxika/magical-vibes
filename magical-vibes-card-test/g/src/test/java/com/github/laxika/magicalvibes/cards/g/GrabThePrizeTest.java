package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrabThePrize.class, Forest.class})
class GrabThePrizeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and damages each opponent when a nonland card is discarded")
    void nonlandDiscardDrawsAndDamagesEachOpponent() {
        harness.setHand(player1, List.of(new GrabThePrize(), new GrabThePrize()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grab the Prize");
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(18);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not damage opponents when a land card is discarded")
    void landDiscardDrawsWithoutDamagingOpponents() {
        harness.setHand(player1, List.of(new GrabThePrize(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(20);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast without another card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new GrabThePrize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Discard is paid before resolution, while drawing and damage wait for resolution")
    void discardIsPaidBeforeResolution() {
        GrabThePrize discarded = new GrabThePrize();
        harness.setHand(player1, List.of(new GrabThePrize(), discarded));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorceryWithDiscard(player1, 0, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can discard a land positioned before the spell in hand")
    void discardsCardBeforeSpellInHand() {
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(discarded, new GrabThePrize()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castSorceryWithDiscard(player1, 1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 20);
    }
}
