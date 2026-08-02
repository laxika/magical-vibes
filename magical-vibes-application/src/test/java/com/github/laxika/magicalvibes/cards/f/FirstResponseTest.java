package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirstResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier during each upkeep when its controller lost life last turn")
    void createsSoldierAfterControllerLostLifeLastTurn() {
        harness.addToBattlefield(player1, new FirstResponse());
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Soldier when its controller did not lose life last turn")
    void doesNotCreateSoldierWithoutLifeLoss() {
        harness.addToBattlefield(player1, new FirstResponse());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }
}
