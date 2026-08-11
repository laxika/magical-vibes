package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Flash lets Raging Kavu be cast during an opponent's turn and haste lets it attack immediately")
    void flashesInAndAttacksImmediately() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RagingKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Raging Kavu");

        harness.forceActivePlayer(player1);
        declareAttackers(player1, List.of(0));

        assertThat(kavu.isAttacking()).isTrue();
    }
}
