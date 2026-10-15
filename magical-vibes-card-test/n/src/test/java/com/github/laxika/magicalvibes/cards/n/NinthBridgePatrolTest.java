package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AcrobaticManeuver;
import com.github.laxika.magicalvibes.cards.f.Fumigate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkywhalersShot;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WeldfastMonitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NinthBridgePatrol.class, GrizzlyBears.class, Unsummon.class,
        AcrobaticManeuver.class, Fumigate.class, SkywhalersShot.class, WeldfastMonitor.class})
class NinthBridgePatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature you control leaves")
    void putsCounterWhenAnotherCreatureYouControlLeaves() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature leaves")
    void doesNotTriggerForOpponentsCreature() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersWhenAnotherCreatureDies() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        Permanent monitor = harness.addToBattlefieldAndReturn(player1, new WeldfastMonitor());
        harness.setLibrary(player1, List.of(new WeldfastMonitor()));
        harness.setHand(player1, List.of(new SkywhalersShot()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, monitor.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Weldfast Monitor");
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersOnceWhenAnotherCreatureIsExiledAndReturned() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        Permanent monitor = harness.addToBattlefieldAndReturn(player1, new WeldfastMonitor());
        harness.setLibrary(player1, List.of(new WeldfastMonitor()));
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, monitor.getId());
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Weldfast Monitor");
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void getsOneCounterForEachSeparateDeparture() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WeldfastMonitor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WeldfastMonitor());
        harness.setLibrary(player1, List.of(new WeldfastMonitor(), new WeldfastMonitor()));
        harness.setHand(player1, List.of(new AcrobaticManeuver(), new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForItsOwnDeparture() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        harness.setLibrary(player1, List.of(new WeldfastMonitor()));
        harness.setHand(player1, List.of(new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, patrol.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(returned -> {
                    assertThat(returned.getId()).isNotEqualTo(patrol.getId());
                    assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                });
    }

    @Test
    void pendingTriggerDoesNotPutCountersOnReturnedPatrol() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        Permanent monitor = harness.addToBattlefieldAndReturn(player1, new WeldfastMonitor());
        harness.setLibrary(player1, List.of(new WeldfastMonitor(), new WeldfastMonitor()));
        harness.setHand(player1, List.of(new AcrobaticManeuver(), new AcrobaticManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, monitor.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, patrol.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof NinthBridgePatrol)
                .singleElement().satisfies(returned ->
                        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForEachOtherCreatureThatDiesSimultaneouslyWithIt() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NinthBridgePatrol());
        harness.addToBattlefield(player1, new WeldfastMonitor());
        harness.addToBattlefield(player1, new WeldfastMonitor());
        harness.addToBattlefield(player2, new WeldfastMonitor());
        harness.setHand(player1, List.of(new Fumigate()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2).allSatisfy(trigger ->
                assertThat(trigger.getSourcePermanentId()).isEqualTo(patrol.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
