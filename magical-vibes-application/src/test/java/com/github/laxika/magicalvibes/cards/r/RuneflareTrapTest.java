package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuneflareTrapTest extends BaseCardTest {

    @Test
    @DisplayName("The alternate cost requires an opponent to have drawn three cards")
    void alternateCostRequiresOpponentDraws() {
        harness.setHand(player1, List.of(new RuneflareTrap()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, player2.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals damage equal to the target player's hand size")
    void alternateCostDealsDamageEqualToTargetHandSize() {
        harness.setHand(player1, List.of(
                new RuneflareTrap(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Divination(), new Divination(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.passPriority(player2);
        harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }
}
