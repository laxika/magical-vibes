package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheThing.class, BurstOfStrength.class, FountainOfYouth.class, GrizzlyBears.class})
class TheThingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts four +1/+1 counters on itself at beginning of combat after a noncreature spell")
    void putsCountersAfterCastingNoncreatureSpell() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not put counters on itself when only a creature spell was cast")
    void doesNotPutCountersAfterCastingCreatureSpell() {
        Permanent thing = addCreatureReady(player1, new TheThing());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paying the attack trigger doubles every counter kind on chosen controlled permanents")
    void payingAttackTriggerDoublesCountersOnChosenPermanents() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bear.setCounterCount(CounterType.CHARGE, 3);
        fountain.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(indexOf(thing)));
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId(), fountain.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bear.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(fountain.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger cannot target an opponent's permanent")
    void cannotTargetOpponentPermanent() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        declareAttackers(List.of(indexOf(thing)));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(opponentPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
