package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetchedWretch.class})
class RetchedWretchTest extends BaseCardTest {

    @Test
    @DisplayName("Returns after dying with a -1/-1 counter and does not trigger again after losing its abilities")
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

    @Test
    @DisplayName("Returns untapped without counters when -1/-1 counters reduce its toughness to zero")
    void returnsAfterLethalCounters() {
        Permanent wretch = harness.addToBattlefieldAndReturn(player1, new RetchedWretch());
        wretch.tap();
        wretch.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent returned = findPermanentOrNull(player1);
        assertThat(returned).isNotNull();
        assertThat(returned.getId()).isNotEqualTo(wretch.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Retched Wretch");
    }

    @Test
    @DisplayName("Returns to its owner rather than its controller")
    void returnsToOwner() {
        Permanent wretch = harness.addToBattlefieldAndReturn(player2, new RetchedWretch());
        gd.stolenCreatures.put(wretch.getId(), player1.getId());
        wretch.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        kill(wretch);

        harness.assertOnBattlefield(player1, "Retched Wretch");
        harness.assertNotOnBattlefield(player2, "Retched Wretch");
        harness.assertNotInGraveyard(player1, "Retched Wretch");
        harness.assertNotInGraveyard(player2, "Retched Wretch");
    }

    @Test
    @DisplayName("Cannot return after its card is removed from the graveyard before resolution")
    void doesNotReturnFromExile() {
        Permanent wretch = harness.addToBattlefieldAndReturn(player1, new RetchedWretch());
        wretch.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        wretch.setMarkedDamage(wretch.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Retched Wretch");
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(wretch.getCard()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Retched Wretch");
        harness.assertNotInGraveyard(player1, "Retched Wretch");
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
