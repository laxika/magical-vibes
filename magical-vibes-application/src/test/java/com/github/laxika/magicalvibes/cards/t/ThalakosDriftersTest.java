package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThalakosDrifters.class, RagingGoblin.class})
class ThalakosDriftersTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card grants shadow until end of turn")
    void discardCardGrantsShadow() {
        Permanent drifters = harness.addToBattlefieldAndReturn(player1, new ThalakosDrifters());
        harness.setHand(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drifters, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefieldAndReturn(player1, new ThalakosDrifters());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        Permanent drifters = harness.addToBattlefieldAndReturn(player1, new ThalakosDrifters());
        harness.setHand(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, drifters, Keyword.SHADOW)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drifters, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Can activate the ability more than once in the same turn")
    void canActivateMoreThanOnceInSameTurn() {
        Permanent drifters = harness.addToBattlefieldAndReturn(player1, new ThalakosDrifters());
        harness.setHand(player1, List.of(new RagingGoblin(), new RagingGoblin()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, drifters, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Only Thalakos Drifters gains shadow")
    void onlySourceGainsShadow() {
        Permanent drifters = harness.addToBattlefieldAndReturn(player1, new ThalakosDrifters());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new RagingGoblin()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drifters, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.SHADOW)).isFalse();
    }
}
