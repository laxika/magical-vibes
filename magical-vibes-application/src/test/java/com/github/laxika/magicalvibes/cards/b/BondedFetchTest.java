package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondedFetch.class, BlindPhantasm.class})
class BondedFetchTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Bonded Fetch draws a card, then prompts for a discard")
    void tappingDrawsThenDiscards() {
        Permanent fetch = addReadyFetch();
        Card discarded = new BlindPhantasm();
        Card drawn = new BondedFetch();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);

        assertThat(fetch.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Blind Phantasm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bonded Fetch can discard the card it draws when the hand starts empty")
    void canDiscardDrawnCardWithEmptyHand() {
        addReadyFetch();
        Card drawn = new BlindPhantasm();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Blind Phantasm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bonded Fetch cannot activate its ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent fetch = addReadyFetch();
        fetch.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyFetch() {
        return addCreatureReady(player1, new BondedFetch());
    }
}
