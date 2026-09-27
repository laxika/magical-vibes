package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThalisseReverentMedium.class, RaiseTheAlarm.class})
class ThalisseReverentMediumTest extends BaseCardTest {

    @Test
    void createsOneSpiritForEachTokenCreatedThisTurn() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        castRaiseTheAlarm();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void createsNoSpiritsWhenNoTokensWereCreatedThisTurn() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void castRaiseTheAlarm() {
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
