package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtCourier.class, DrossCrocodile.class})
class ThoughtCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Thought Courier and goes on the stack")
    void activatingTapsAndStacks() {
        Permanent courier = addCreatureReady(player1, new ThoughtCourier());

        harness.activateAbility(player1, 0, null, null);

        assertThat(courier.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent courier = addCreatureReady(player1, new ThoughtCourier());
        courier.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Resolving draws a card then prompts for discard")
    void resolvingDrawsThenPromptsDiscard() {
        addCreatureReady(player1, new ThoughtCourier());
        harness.setHand(player1, List.of(new DrossCrocodile()));
        harness.setLibrary(player1, List.of(new DrossCrocodile()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Completing discard moves card to graveyard, net hand size unchanged")
    void completingDiscard() {
        addCreatureReady(player1, new ThoughtCourier());
        DrossCrocodile discarded = new DrossCrocodile();
        DrossCrocodile drawn = new DrossCrocodile();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty starting hand, draws and then discards the drawn card")
    void drawsThenDiscardsDrawnCardWithEmptyStartingHand() {
        addCreatureReady(player1, new ThoughtCourier());
        DrossCrocodile drawn = new DrossCrocodile();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Looting with empty deck and empty hand skips discard")
    void emptyDeckAndHandSkipsDiscard() {
        addCreatureReady(player1, new ThoughtCourier());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
