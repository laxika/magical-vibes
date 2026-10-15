package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriffnautTracker.class, HillGiant.class, LightningBolt.class})
class GriffnautTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two target cards from a single graveyard when it enters")
    void exilesCardsFromSingleGraveyardOnEnter() {
        Card first = new HillGiant();
        Card second = new LightningBolt();
        Card untouched = new HillGiant();
        harness.setGraveyard(player2, List.of(first, second, untouched));

        castGriffnautTracker();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Rejects selecting cards from different graveyards")
    void targetsMustShareOneGraveyard() {
        Card ownCard = new HillGiant();
        Card opponentCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castGriffnautTracker();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownCard.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Allows choosing fewer than two cards")
    void canChooseFewerThanTwoCards() {
        Card chosen = new HillGiant();
        Card untouched = new LightningBolt();
        harness.setGraveyard(player1, List.of(chosen, untouched));

        castGriffnautTracker();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
    }

    @Test
    @DisplayName("Allows choosing zero cards even when graveyards contain cards")
    void canChooseZeroCards() {
        Card untouched = new GriffnautTracker();
        harness.setGraveyard(player2, List.of(untouched));

        castGriffnautTracker();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Griffnaut Tracker");
    }

    @Test
    @DisplayName("Resolves without a target choice when both graveyards are empty")
    void resolvesWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castGriffnautTracker();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Griffnaut Tracker");
    }

    @Test
    @DisplayName("Cannot choose more than two cards")
    void rejectsThreeTargets() {
        Card first = new GriffnautTracker();
        Card second = new GriffnautTracker();
        Card third = new GriffnautTracker();
        harness.setGraveyard(player2, List.of(first, second, third));

        castGriffnautTracker();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Cannot choose the same graveyard card twice")
    void rejectsDuplicateTargets() {
        Card first = new GriffnautTracker();
        Card second = new GriffnautTracker();
        harness.setGraveyard(player2, List.of(first, second));

        castGriffnautTracker();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Exiles the remaining legal target when one target leaves the graveyard")
    void resolvesWithOneRemainingTarget() {
        Card departed = new GriffnautTracker();
        Card remaining = new GriffnautTracker();
        harness.setGraveyard(player2, List.of(departed, remaining));

        castGriffnautTracker();
        harness.handleMultipleCardsChosen(player1, List.of(departed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(departed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remaining);
        harness.assertInHand(player2, "Griffnaut Tracker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not exile departed targets or choose replacements when all targets leave")
    void doesNotRetargetWhenAllTargetsLeave() {
        Card departed = new GriffnautTracker();
        Card untouched = new GriffnautTracker();
        harness.setGraveyard(player2, List.of(departed, untouched));

        castGriffnautTracker();
        harness.handleMultipleCardsChosen(player1, List.of(departed.getId()));
        harness.setGraveyard(player2, List.of(untouched));
        harness.setHand(player2, List.of(departed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Griffnaut Tracker");
        assertThat(gd.stack).isEmpty();
    }

    private void castGriffnautTracker() {
        harness.castFromHand(player1, new GriffnautTracker(), "{3}{W}");
        harness.passBothPriorities();
    }
}
