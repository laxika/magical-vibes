package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class MoonlitWakeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 1 life when an opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new MoonlitWake());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller gains 1 life when their creature dies")
    void gainsLifeWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new MoonlitWake());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }
}
