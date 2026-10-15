package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({PlaguemawBeast.class, GrizzlyBears.class, LlanowarElves.class})
class PlaguemawBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices chosen creature and proliferates -1/-1 counters")
    void sacrificesCreatureAndProliferatesMinusCounters() {
        addReadyBeast(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enemyBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null); // sacrifice Llanowar Elves
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities(); // resolve ability

        // Choose enemy bears for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(enemyBears.getId()));

        // Llanowar Elves should be sacrificed
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");

        // Bears should have 2 -1/-1 counters
        assertThat(enemyBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices chosen creature and proliferates +1/+1 counters")
    void sacrificesCreatureAndProliferatesPlusCounters() {
        addReadyBeast(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        Permanent allyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        allyBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(allyBears.getId()));

        assertThat(allyBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice itself to activate ability")
    void canSacrificeItself() {
        addReadyBeast(player1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null); // sacrifice itself (auto-pay, only creature)
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player1, "Plaguemaw Beast");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent beast = addReadyBeast(player1);
        beast.tap();
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new PlaguemawBeast());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        addReadyBeast(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();

        // Choose nothing
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Auto-sacrifices when only one creature is available")
    void autoSacrificesWhenOnlyOneCreature() {
        addReadyBeast(player1);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Beast should be auto-sacrificed (only creature controlled by player1)
        harness.assertNotOnBattlefield(player1, "Plaguemaw Beast");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Proliferates selected players and all their existing counter kinds")
    void proliferatesPlayerCounters() {
        addReadyBeast(player1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        harness.assertInGraveyard(player1, "Plaguemaw Beast");
    }

    @Test
    @DisplayName("Adds one of each existing counter kind to every selected permanent")
    void proliferatesMultiplePermanentsAndCounterKinds() {
        addReadyBeast(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new PlaguemawBeast());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PlaguemawBeast());
        Permanent unselected = harness.addToBattlefieldAndReturn(player2, new PlaguemawBeast());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        first.setCounterCount(CounterType.CHARGE, 3);
        second.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(first.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves normally when no permanents or players have counters")
    void resolvesWithoutEligibleCounters() {
        addReadyBeast(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Plaguemaw Beast");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    private Permanent addReadyBeast(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PlaguemawBeast());
        perm.setSummoningSick(false);
        return perm;
    }
}
