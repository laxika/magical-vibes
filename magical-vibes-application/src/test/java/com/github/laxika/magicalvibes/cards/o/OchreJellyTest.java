package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OchreJelly.class, Assassinate.class})
class OchreJellyTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OchreJelly()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent jelly = findPermanent(player1, "Ochre Jelly");
        assertThat(jelly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates a delayed copy with half its death counters")
    void createsDelayedCopyWithHalfDeathCounters() {
        Permanent jelly = addCreatureReady(player1, new OchreJelly());
        jelly.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        destroyWithAssassinate(jelly);

        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).hasSize(1);
        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        List<Permanent> copies = findPermanents(player1, "Ochre Jelly");
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().isToken()).isTrue();
        assertThat(copies.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger with fewer than two +1/+1 counters")
    void doesNotTriggerBelowCounterThreshold() {
        Permanent jelly = addCreatureReady(player1, new OchreJelly());
        jelly.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithAssassinate(jelly);

        assertThat(gd.getDelayedActions(DelayedCreateTokenCopy.class)).isEmpty();
        assertThat(findPermanents(player1, "Ochre Jelly")).isEmpty();
    }

    private void destroyWithAssassinate(Permanent target) {
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
