package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaddiOffshoot.class, Forest.class})
@DisplayName("Jaddi Offshoot")
class JaddiOffshootTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when a land you control enters")
    void gainsLifeOnControllerLandfall() {
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's land")
    void doesNotTriggerForOpponentLandfall() {
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
