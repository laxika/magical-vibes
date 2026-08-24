package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushOfDread.class, GrizzlyBears.class})
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

    private void cast(int[] modes, int totalMana, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new RushOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targets, null);
        harness.passBothPriorities();
    }
}
