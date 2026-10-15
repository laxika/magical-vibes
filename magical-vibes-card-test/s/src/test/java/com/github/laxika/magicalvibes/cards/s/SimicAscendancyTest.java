package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicAscendancy.class, GrizzlyBears.class, JundCharm.class, Spellbook.class})
class SimicAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability puts a +1/+1 counter on a creature you control and adds a growth counter")
    void activatedAbilityBuildsGrowthCounters() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, bearsId);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ascendancy.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds two growth counters when two +1/+1 counters are put on a creature at once")
    void preservesMultipleCountersPlacedAtOnce() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JundCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, 2, bearsId);
        resolveAllTriggers();

        assertThat(ascendancy.getCounterCount(CounterType.GROWTH)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature controlled by an opponent")
    void ignoresCountersOnOpponentsCreature() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new JundCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player2, 0, 2, bearsId);
        resolveAllTriggers();

        assertThat(ascendancy.getCounterCount(CounterType.GROWTH)).isZero();
    }

    @Test
    @DisplayName("Wins at the beginning of upkeep with twenty growth counters")
    void winsWithTwentyGrowthCountersAtUpkeep() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        ascendancy.setCounterCount(CounterType.GROWTH, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SimicAscendancy());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new SimicAscendancy());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Triggers when an opponent puts counters on a creature you control")
    void countsCountersPlacedByOpponent() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new JundCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, 2, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ascendancy.getCounterCount(CounterType.GROWTH)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with only nineteen growth counters")
    void doesNotTriggerBelowThreshold() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        ascendancy.setCounterCount(CounterType.GROWTH, 19);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        ascendancy.setCounterCount(CounterType.GROWTH, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Rechecks growth counters when the upkeep trigger resolves")
    void doesNotWinIfCountersDropBelowThreshold() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        ascendancy.setCounterCount(CounterType.GROWTH, 20);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        ascendancy.setCounterCount(CounterType.GROWTH, 19);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Reaching twenty growth counters does not win before upkeep")
    void reachingTwentyCountersWaitsForUpkeep() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new SimicAscendancy());
        ascendancy.setCounterCount(CounterType.GROWTH, 19);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        resolveAllTriggers();

        assertThat(ascendancy.getCounterCount(CounterType.GROWTH)).isEqualTo(20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
