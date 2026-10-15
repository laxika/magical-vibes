package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DemonicCounsel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnivorousFlytrap.class, GrizzlyBears.class, Forest.class, Shock.class,
        Pacifism.class, LeoninScimitar.class, DemonicCounsel.class})
class OmnivorousFlytrapTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes two counters when delirium is met")
    void etbDistributesCountersWithDelirium() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, fourCardTypes());

        castFlytrap();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with six graveyard card types doubles the distributed counters")
    void attackDoublesDistributedCountersWithSixCardTypes() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, sixCardTypes());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack ability does not trigger without delirium")
    void attackDoesNotTriggerWithoutDelirium() {
        addCreatureReady(player1, new OmnivorousFlytrap());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void etbDoesNotTriggerWithoutDeliriumEvenIfOpponentHasDelirium() {
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new DemonicCounsel()));
        harness.setGraveyard(player2, sixCardTypes());

        castFlytrap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Omnivorous Flytrap")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackDistributesOneCounterToEachTargetWithFourTypes() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, fourCardTypes());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(flytrap.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void etbWithSixTypesDoublesAllCountersOnBothTargets() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        firstTarget.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        secondTarget.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setGraveyard(player1, sixCardTypes());

        castFlytrap();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(findPermanent(player1, "Omnivorous Flytrap")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackDoesNothingIfDeliriumIsLostBeforeResolution() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        harness.setGraveyard(player1, fourCardTypes());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(flytrap.getId()));
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new DemonicCounsel()));
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackChecksSixTypesAtResolutionAndDoublesExistingCounters() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        flytrap.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setGraveyard(player1, fourCardTypes());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(flytrap.getId()));
        harness.setGraveyard(player1, sixCardTypes());
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    void attackDoesNotDoubleWhenSixTypesAreLostBeforeResolution() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        flytrap.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setGraveyard(player1, sixCardTypes());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(flytrap.getId()));
        harness.setGraveyard(player1, fourCardTypes());
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void countersAssignedToMissingTargetAreNotRedistributed() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        Permanent otherTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, sixCardTypes());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(flytrap.getId(), otherTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(otherTarget);
        harness.setGraveyard(player2, List.of(otherTarget.getCard()));
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castFlytrap() {
        harness.castFromHand(player1, new OmnivorousFlytrap(), "{2}{G}");
    }

    private List<com.github.laxika.magicalvibes.model.Card> fourCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Pacifism());
    }

    private List<com.github.laxika.magicalvibes.model.Card> sixCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Pacifism(),
                new LeoninScimitar(), new DemonicCounsel());
    }
}
