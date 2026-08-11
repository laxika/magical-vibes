package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetchedWretchTest extends BaseCardTest {

    @Test
    @DisplayName("Returns with a -1/-1 counter and does not trigger again after losing its abilities")
    void returnsAndLosesAbilitiesIndefinitely() {
        Permanent wretch = harness.addToBattlefieldAndReturn(player1, new RetchedWretch());
        wretch.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        kill(wretch);

        Permanent returned = findPermanentOrNull(player1);
        assertThat(returned).isNotNull();
        harness.assertNotInGraveyard(player1, "Retched Wretch");

        returned.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        returned.setMarkedDamage(returned.getEffectiveToughness());
        harness.runStateBasedActions();

        assertThat(findPermanentOrNull(player1)).isNull();
        harness.assertInGraveyard(player1, "Retched Wretch");
    }

    @Test
    @DisplayName("Does not return when it dies without a -1/-1 counter")
    void doesNotReturnWithoutMinusOneCounter() {
        Permanent wretch = harness.addToBattlefieldAndReturn(player1, new RetchedWretch());

        kill(wretch);

        assertThat(findPermanentOrNull(player1)).isNull();
        harness.assertInGraveyard(player1, "Retched Wretch");
    }

    private void kill(Permanent wretch) {
        wretch.setMarkedDamage(wretch.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }

    private Permanent findPermanentOrNull(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Retched Wretch"))
                .findFirst()
                .orElse(null);
    }
}
