package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CobraTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four Snake tokens after an opponent destroys your noncreature permanent")
    void createsFourSnakesAfterOpponentDestruction() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new CobraTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passPriority(player2);
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(4);
    }

    @Test
    @DisplayName("The alternate cost requires an opponent to have destroyed a noncreature permanent")
    void alternateCostRequiresOpponentDestruction() {
        harness.setHand(player1, List.of(new CobraTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destruction by your own spell does not enable the alternate cost")
    void ownDestructionDoesNotEnableAlternateCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Shatter(), new CobraTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
