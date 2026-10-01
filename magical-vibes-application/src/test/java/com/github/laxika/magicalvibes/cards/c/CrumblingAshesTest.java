package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NoggleBandit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrumblingAshes.class, NoggleBandit.class})
class CrumblingAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger destroys the targeted creature with a -1/-1 counter")
    void destroysCreatureWithCounter() {
        addCreatureReady(player1, new CrumblingAshes());
        Permanent bandit = addCreatureReady(player2, new NoggleBandit());
        bandit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bandit.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Noggle Bandit");
        harness.assertInGraveyard(player2, "Noggle Bandit");
    }

    @Test
    @DisplayName("Does not trigger when no creature has a -1/-1 counter")
    void doesNotTriggerWithoutCounter() {
        addCreatureReady(player1, new CrumblingAshes());
        addCreatureReady(player2, new NoggleBandit());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Creature without a -1/-1 counter is not a legal target")
    void creatureWithoutCounterNotTargetable() {
        addCreatureReady(player1, new CrumblingAshes());
        Permanent withCounter = addCreatureReady(player2, new NoggleBandit());
        withCounter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent withoutCounter = addCreatureReady(player2, new NoggleBandit());

        advanceToUpkeep(player1);

        // The creature without a -1/-1 counter is not a legal target and is rejected
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, withoutCounter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger for a creature with a different counter type")
    void doesNotTriggerForDifferentCounterType() {
        addCreatureReady(player1, new CrumblingAshes());
        addCreatureReady(player2, new NoggleBandit())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not destroy the creature if its -1/-1 counter is removed before resolution")
    void targetBecomesIllegalBeforeResolution() {
        addCreatureReady(player1, new CrumblingAshes());
        Permanent bandit = addCreatureReady(player2, new NoggleBandit());
        bandit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bandit.getId());
        bandit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Noggle Bandit");
        harness.assertNotInGraveyard(player2, "Noggle Bandit");
    }
}
