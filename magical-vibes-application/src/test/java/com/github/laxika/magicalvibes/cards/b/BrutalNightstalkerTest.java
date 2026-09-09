package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrutalNightstalker.class, BearCub.class})
class BrutalNightstalkerTest extends BaseCardTest {

    private void castBrutalNightstalker() {
        harness.castFromHand(player1, new BrutalNightstalker(), "{3}{B}{B}");
    }

    @Test
    @DisplayName("The ETB ability only offers opponents as valid targets")
    void targetFilterExcludesController() {
        castBrutalNightstalker();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Accepting the may ability makes target opponent discard a card")
    void acceptingMayMakesOpponentDiscard() {
        harness.setHand(player2, List.of(new BearCub()));
        castBrutalNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Declining the may ability leaves the opponent's hand untouched")
    void decliningMayLeavesHandUntouched() {
        harness.setHand(player2, List.of(new BearCub()));
        castBrutalNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting the may ability does nothing when opponent has an empty hand")
    void acceptingMayDoesNothingWithEmptyHand() {
        harness.setHand(player2, List.of());
        castBrutalNightstalker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
