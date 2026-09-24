package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtPartition.class, GrizzlyBears.class})
class ThoughtPartitionTest extends BaseCardTest {

    @Test
    void selectedNonlandCardStaysInHandAndPerpetuallyChangesCharacteristics() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new ThoughtPartition()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        Card modifiedBears = gd.playerHands.get(player2.getId()).getFirst();
        assertThat(modifiedBears).isNotSameAs(bears);
        assertThat(modifiedBears.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(modifiedBears.getColors()).containsExactly(CardColor.WHITE);
        assertThat(modifiedBears.getManaCost()).isEqualTo("{5}");

        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == modifiedBears);
    }
}
