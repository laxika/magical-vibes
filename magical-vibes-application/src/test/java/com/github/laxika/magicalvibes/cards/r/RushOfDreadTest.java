package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushOfDread.class, GrizzlyBears.class, SterlingHound.class})
class RushOfDreadTest extends BaseCardTest {

    @Test
    void sacrificesHalfCreaturesRoundedUp() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, 4, List.of(player2.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void discardsHalfHandRoundedUp() {
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        cast(new int[]{1}, 5, List.of(player2.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void losesHalfLifeRoundedUp() {
        harness.setLife(player2, 7);

        cast(new int[]{2}, 5, List.of(player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void modesRequireAnOpponentTarget() {
        assertThatThrownBy(() -> cast(new int[]{2}, 5, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allModesResolveInPrintedOrderAfterPayingAllAdditionalCosts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.setHand(player2, List.of(
                new SterlingHound(), new SterlingHound(), new SterlingHound(),
                new SterlingHound(), new SterlingHound()));
        harness.setLife(player2, 7);

        cast(new int[]{2, 1, 0}, 8, List.of(player2.getId(), player2.getId(), player2.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        harness.assertLife(player2, 7);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertLife(player2, 7);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        harness.assertLife(player2, 3);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Rush of Dread");
    }

    @Test
    void emptyBattlefieldAndHandDoNotPreventTheLifeLossMode() {
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        cast(new int[]{0, 1, 2}, 8, List.of(player2.getId(), player2.getId(), player2.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player1, "Rush of Dread");
    }

    @Test
    void aSingleCreatureAndSingleHandCardAreBothRemoved() {
        harness.addToBattlefield(player2, new SterlingHound());
        harness.setHand(player2, List.of(new SterlingHound()));

        cast(new int[]{0, 1}, 6, List.of(player2.getId(), player2.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificesExactlyHalfAnEvenCreatureCountAndLeavesCastersCreaturesAlone() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        cast(new int[]{0}, 4, List.of(player2.getId()));
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), fourth.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second, third);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(own);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void discardsExactlyHalfAnEvenHandCount() {
        harness.setHand(player2, List.of(
                new SterlingHound(), new SterlingHound(), new SterlingHound(), new SterlingHound()));

        cast(new int[]{1}, 5, List.of(player2.getId()));
        harness.handleCardChosen(player2, 3);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void amountsUseTheOpponentStateAtResolutionRatherThanAtCasting() {
        harness.addToBattlefield(player2, new SterlingHound());
        harness.setHand(player2, List.of(new SterlingHound()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RushOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(player2.getId(), player2.getId(), player2.getId()), null);

        Permanent second = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.setHand(player2, List.of(new SterlingHound(), new SterlingHound(), new SterlingHound()));
        harness.setLife(player2, 9);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId(), third.getId()));
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertLife(player2, 4);
    }

    @ParameterizedTest
    @CsvSource({"0, 3", "1, 4", "2, 4"})
    void eachModeRequiresItsAdditionalManaCost(int mode, int insufficientMana) {
        assertThatThrownBy(() -> cast(new int[]{mode}, insufficientMana, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combinedModesRequireTheSumOfTheirAdditionalManaCosts() {
        assertThatThrownBy(() -> cast(new int[]{0, 1, 2}, 7,
                List.of(player2.getId(), player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, int totalMana, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new RushOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targets, null);
        harness.passBothPriorities();
    }
}
