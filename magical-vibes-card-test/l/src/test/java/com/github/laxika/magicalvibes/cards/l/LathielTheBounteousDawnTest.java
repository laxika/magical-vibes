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

import java.util.ArrayList;
import java.util.List;

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


    @Test
    @DisplayName("Triggers on an opponent's end step and can put counters on their creature")
    void triggersOnOpponentEndStep() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player2);

        harness.handlePermanentChosen(player1, target.getId());
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent gaining life does not satisfy the trigger condition")
    void opponentLifeGainDoesNotTrigger() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player2.getId(), 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May choose no targets even when another creature is available")
    void mayChooseNoTargets() {
        Permanent lathiel = addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lathiel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Additional life gained after announcing the ability does not increase its counters")
    void distributionIsFixedBeforeResolution() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        harness.handlePermanentChosen(player1, target.getId());
        harness.handleListChoice(player1, "3");
        gd.lifeGainedThisTurn.put(player1.getId(), 8);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters assigned to a departed target are not redistributed")
    void departedTargetDoesNotChangeDistribution() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent survivingTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent departedTarget = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);

        harness.handlePermanentChosen(player1, survivingTarget.getId());
        harness.handlePermanentChosen(player1, departedTarget.getId());
        harness.handleListChoice(player1, "1");
        PendingInteraction.ColorChoice lastAssignment =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(lastAssignment.options()).containsExactly("1", "2");
        harness.handleListChoice(player1, "2");
        gd.playerBattlefields.get(player2.getId()).remove(departedTarget);
        gd.playerGraveyards.get(player2.getId()).add(departedTarget.getCard());
        harness.passBothPriorities();

        assertThat(survivingTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(departedTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Life loss does not subtract from the amount of life gained")
    void lifeLossDoesNotReduceCounters() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 5);
        harness.setLife(player1, 18);

        advanceToEndStep(player1);

        harness.handlePermanentChosen(player1, target.getId());
        harness.handleListChoice(player1, "5");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Lifelink combat damage supplies life for the end-step distribution")
    void lifelinkDamageEnablesDistribution() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);

        advanceToEndStep(player1);

        harness.handlePermanentChosen(player1, target.getId());
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can distribute counters among one hundred other creatures")
    void canChooseMoreThanNinetyNineTargets() {
        addCreatureReady(player1, new LathielTheBounteousDawn());
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(addCreatureReady(player1, new GrizzlyBears()));
        }
        gd.lifeGainedThisTurn.put(player1.getId(), 100);

        advanceToEndStep(player1);

        for (Permanent target : targets) {
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validPermanentIds()).contains(target.getId());
            harness.handlePermanentChosen(player1, target.getId());
        }
        for (int i = 0; i < 100; i++) {
            harness.handleListChoice(player1, "1");
        }
        harness.passBothPriorities();

        assertThat(targets).allSatisfy(target ->
                assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
