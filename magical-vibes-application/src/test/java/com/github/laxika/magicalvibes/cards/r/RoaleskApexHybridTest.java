package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({RoaleskApexHybrid.class, GrizzlyBears.class, DoomBlade.class})
class RoaleskApexHybridTest extends BaseCardTest {

    @Test
    @DisplayName("When Roalesk enters, it puts two +1/+1 counters on another creature you control")
    void etbPutsTwoCountersOnAnotherCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castRoalesk(bears.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Roalesk cannot target an opponent's creature for its enter-the-battlefield ability")
    void etbCannotTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoaleskApexHybrid()));
        addRoaleskMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("When Roalesk dies, it proliferates twice")
    void deathProliferatesTwice() {
        harness.addToBattlefield(player1, new RoaleskApexHybrid());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killRoalesk();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each proliferation can choose different creatures, including opposing creatures")
    void deathAllowsIndependentSelections() {
        harness.addToBattlefield(player1, new RoaleskApexHybrid());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killRoalesk();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opposingBears.getId()));

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing nothing for the first proliferation still allows the second")
    void deathAllowsEmptyFirstSelection() {
        harness.addToBattlefield(player1, new RoaleskApexHybrid());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killRoalesk();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both proliferations add every existing kind of counter to a chosen player")
    void deathProliferatesAllPlayerCounterTypes() {
        harness.addToBattlefield(player1, new RoaleskApexHybrid());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);

        killRoalesk();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature with zero toughness after the first proliferation survives until both finish")
    void deathDefersStateBasedActionsUntilBothProliferationsFinish() {
        harness.addToBattlefield(player1, new RoaleskApexHybrid());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        killRoalesk();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Roalesk enters without counters when there is no other creature to target")
    void etbDoesNotTargetItselfWhenAlone() {
        harness.setHand(player1, List.of(new RoaleskApexHybrid()));
        addRoaleskMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roalesk, Apex Hybrid");
        Permanent roalesk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(roalesk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castRoalesk(UUID targetId) {
        harness.setHand(player1, List.of(new RoaleskApexHybrid()));
        addRoaleskMana();
        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addRoaleskMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void killRoalesk() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Roalesk, Apex Hybrid"));
    }
}
