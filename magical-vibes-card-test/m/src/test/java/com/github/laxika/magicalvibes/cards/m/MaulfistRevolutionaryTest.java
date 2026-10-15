package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaulfistRevolutionary.class, Forest.class, DoomBlade.class})
class MaulfistRevolutionaryTest extends BaseCardTest {

    @Test
    void entersAndAddsOneCounterOfEachKindToTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setCounterCount(CounterType.CHARGE, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void diesAndAddsOneCounterOfEachKindToTargetPlayer() {
        Permanent revolutionary = harness.addToBattlefieldAndReturn(player1, new MaulfistRevolutionary());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, revolutionary.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void entersAndAddsRadiationCounterToTargetPlayer() {
        gd.playerRadCounters.put(player2.getId(), 2);

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void entersAndAddsExperienceCounterToTargetPlayer() {
        gd.playerExperienceCounters.put(player1.getId(), 2);

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void entersAndAddsSparkCounterToTargetPlayer() {
        gd.playerSparkCounters.put(player1.getId(), 2);

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void counterlessPermanentCanBeTargetedWithoutReceivingCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesCounterKindsPresentAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.setCounterCount(CounterType.CHARGE, 2);

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        target.setCounterCount(CounterType.CHARGE, 0);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
