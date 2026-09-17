package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LathielTheBounteousDawn.class, GrizzlyBears.class})
class LathielTheBounteousDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Distributes up to life gained among other target creatures")
    void distributesUpToLifeGainedAmongOtherCreatures() {
        Permanent lathiel = addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTarget = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(ownTarget.getId(), opponentTarget.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(lathiel.getId());

        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.ColorChoice counterChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(counterChoice.options()).containsExactly("1", "2", "3");
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        assertThat(ownTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lathiel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when its controller did not gain life")
    void doesNotTriggerWithoutLifeGain() {
        Permanent lathiel = addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lathiel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
