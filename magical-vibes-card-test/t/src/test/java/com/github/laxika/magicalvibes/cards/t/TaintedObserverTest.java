package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaintedObserver.class, Cankerbloom.class})
class TaintedObserverTest extends BaseCardTest {

    @Test
    void combatDamageGivesOnePoisonCounterWithoutUsingTheStack() {
        harness.setLife(player2, 20);
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new TaintedObserver());
        observer.setSummoningSick(false);
        observer.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingTwoManaAfterAnotherCreatureEntersProliferates() {
        Permanent bears = addCounteredBears();
        harness.addToBattlefield(player1, new TaintedObserver());
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void decliningPaymentDoesNotProliferate() {
        Permanent bears = addCounteredBears();
        harness.addToBattlefield(player1, new TaintedObserver());
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new TaintedObserver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerForOpponentCreatureEntry() {
        harness.addToBattlefield(player1, new TaintedObserver());

        harness.enterBattlefieldAndReturn(player2, new Cankerbloom());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void proliferatesAllCounterKindsOnChosenPermanentsAndPlayers() {
        Permanent creature = addCounteredBears();
        creature.setCounterCount(CounterType.OIL, 2);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addToBattlefield(player1, new TaintedObserver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.enterBattlefieldAndReturn(player1, new Cankerbloom());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void mayChooseNoPermanentsOrPlayersAfterPaying() {
        Permanent creature = addCounteredBears();
        harness.addToBattlefield(player1, new TaintedObserver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.enterBattlefieldAndReturn(player1, new Cankerbloom());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void insufficientManaDoesNotProliferate() {
        Permanent creature = addCounteredBears();
        harness.addToBattlefield(player1, new TaintedObserver());
        harness.setHand(player1, List.of(new Cankerbloom()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addCounteredBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new Cankerbloom());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bears;
    }
}
