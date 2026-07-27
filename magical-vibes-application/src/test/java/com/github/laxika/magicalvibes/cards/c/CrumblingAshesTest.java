package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrumblingAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger destroys the targeted creature with a -1/-1 counter")
    void destroysCreatureWithCounter() {
        addCreatureReady(player1, new CrumblingAshes());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when no creature has a -1/-1 counter")
    void doesNotTriggerWithoutCounter() {
        addCreatureReady(player1, new CrumblingAshes());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Creature without a -1/-1 counter is not a legal target")
    void creatureWithoutCounterNotTargetable() {
        addCreatureReady(player1, new CrumblingAshes());
        Permanent withCounter = addCreatureReady(player2, new GrizzlyBears());
        withCounter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent withoutCounter = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        // The creature without a -1/-1 counter is not a legal target and is rejected
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, withoutCounter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
