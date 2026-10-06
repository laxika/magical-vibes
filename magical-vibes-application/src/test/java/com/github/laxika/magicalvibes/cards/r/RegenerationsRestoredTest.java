package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RegenerationsRestored.class)
class RegenerationsRestoredTest extends BaseCardTest {

    @Test
    void entersWithTwelveTimeCounters() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new RegenerationsRestored());

        assertThat(source.getCounterCount(CounterType.TIME)).isEqualTo(12);
    }

    @Test
    void removingTimeCounterScriesAndGainsLife() {
        harness.setLibrary(player1, java.util.List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 2);
        harness.setLife(player1, 20);

        source.setCounterCount(CounterType.TIME, 1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Regenerations Restored");
    }

    @Test
    void removingLastTimeCounterExilesItAndQueuesAnExtraTurn() {
        harness.setLibrary(player1, java.util.List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 1);

        source.setCounterCount(CounterType.TIME, 0);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Regenerations Restored");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void upkeepRemovesOneCounterAndGainsOneLifeWithoutExiling() {
        harness.setLibrary(player1, List.of());
        Permanent source = harness.enterBattlefieldAndReturn(player1, new RegenerationsRestored());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.TIME)).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Regenerations Restored");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void removingSeveralCountersAtOnceScriesOnceAndGainsOneLife() {
        RegenerationsRestored top = new RegenerationsRestored();
        RegenerationsRestored bottom = new RegenerationsRestored();
        harness.setLibrary(player1, List.of(top, bottom));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 12);
        harness.setLife(player1, 20);

        source.setCounterCount(CounterType.TIME, 9);
        harness.runStateBasedActions();
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Regenerations Restored");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void removingLastCounterOutsideUpkeepQueuesBothTriggeredAbilities() {
        harness.setLibrary(player1, List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 1);

        source.setCounterCount(CounterType.TIME, 0);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Regenerations Restored");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    void upkeepDoesNotTriggerWhenThereAreNoTimeCounters() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new RegenerationsRestored());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Regenerations Restored");
    }

    @Test
    void counterAbilityChecksCurrentCounterCountWhenItResolves() {
        harness.setLibrary(player1, List.of());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RegenerationsRestored());
        source.setCounterCount(CounterType.TIME, 2);
        harness.setLife(player1, 20);

        source.setCounterCount(CounterType.TIME, 1);
        harness.runStateBasedActions();
        source.setCounterCount(CounterType.TIME, 3);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Regenerations Restored");
        assertThat(gd.extraTurns).isEmpty();
    }
}
