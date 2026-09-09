package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingDrake.class, Forest.class})
class ScreechingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card, then discards a card (net hand size unchanged)")
    void etbDrawThenDiscard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScreechingDrake(), "{3}{U}");

        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve ETB trigger — draws, then prompts discard

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        // Drew 1 (Forest), discarded 1 → hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB draws before asking which card to discard")
    void etbDrawsBeforeDiscarding() {
        Forest cardToKeep = new Forest();
        Forest cardToDraw = new Forest();
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.castFromHand(player1, new ScreechingDrake(), "{3}{U}");
        harness.setHand(player1, List.of(cardToKeep));

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger through the draw

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToKeep, cardToDraw);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardToKeep);
    }
}
