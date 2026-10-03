package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LeafkinDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContractualSafeguard.class, LeafkinDruid.class})
class ContractualSafeguardTest extends BaseCardTest {

    @Test
    void addendumAddsShieldThenCopiesThatCounterKindToOtherCreatures() {
        Permanent first = addCreatureReady(player1, new LeafkinDruid());
        Permanent second = addCreatureReady(player1, new LeafkinDruid());
        castSafeguardInMainPhase();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "shield counters");

        assertThat(first.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void outsideMainPhaseCopiesAnExistingCounterToOtherControlledCreaturesOnly() {
        Permanent reference = addCreatureReady(player1, new LeafkinDruid());
        Permanent other = addCreatureReady(player1, new LeafkinDruid());
        Permanent opponent = addCreatureReady(player2, new LeafkinDruid());
        reference.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castSafeguardOutsideMainPhase();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(reference.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(reference.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    void canChooseADifferentReferenceFromTheCreatureGivenAShield() {
        Permanent shieldRecipient = addCreatureReady(player1, new LeafkinDruid());
        Permanent reference = addCreatureReady(player1, new LeafkinDruid());
        Permanent third = addCreatureReady(player1, new LeafkinDruid());
        reference.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castSafeguardInMainPhase();

        harness.handleMultiplePermanentsChosen(player1, List.of(shieldRecipient.getId()));
        harness.handlePermanentChosen(player1, reference.getId());
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(reference.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(shieldRecipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shieldRecipient.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(reference.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(third.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    void copiesOnlyTheChosenKindAndAddsOneToCreaturesAlreadyHavingIt() {
        Permanent reference = addCreatureReady(player1, new LeafkinDruid());
        Permanent other = addCreatureReady(player1, new LeafkinDruid());
        reference.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        reference.setCounterCount(CounterType.FLYING, 1);
        other.setCounterCount(CounterType.FLYING, 1);
        castSafeguardOutsideMainPhase();

        harness.handlePermanentChosen(player1, reference.getId());
        harness.handleListChoice(player1, "flying counters");

        assertThat(reference.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.FLYING)).isEqualTo(2);
        assertThat(reference.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void outsideMainPhaseWithNoCountersDoesNothing() {
        Permanent creature = addCreatureReady(player1, new LeafkinDruid());
        castSafeguardOutsideMainPhase();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Contractual Safeguard");
    }

    @Test
    void mainPhaseWithNoControlledCreaturesStillResolves() {
        Permanent opponent = addCreatureReady(player2, new LeafkinDruid());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castSafeguardInMainPhase();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponent.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Contractual Safeguard");
    }

    @Test
    void postcombatMainPhaseAlsoGetsAddendumWithOnlyOneCreature() {
        Permanent creature = addCreatureReady(player1, new LeafkinDruid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ContractualSafeguard(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "shield counters");

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Contractual Safeguard");
    }

    private void castSafeguardOutsideMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ContractualSafeguard(), "{2}{W}");
        harness.passBothPriorities();
    }

    private void castSafeguardInMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ContractualSafeguard(), "{2}{W}");
        harness.passBothPriorities();
    }
}
