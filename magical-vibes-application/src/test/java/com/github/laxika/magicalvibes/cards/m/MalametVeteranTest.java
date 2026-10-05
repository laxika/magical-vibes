package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TectonicHazard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalametVeteran.class, ArmoredKincaller.class, TectonicHazard.class, Forest.class})
class MalametVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with four permanent cards in the graveyard puts a counter on target creature")
    void attacksWithDescendPutsCounterOnTargetCreature() {
        addReadyVeteran();
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(
                new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Descend does not count nonpermanent or opponent graveyard cards")
    void attacksWithoutFourOwnPermanentCardsDoNotPutCounter() {
        addReadyVeteran();
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(
                new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller(), new TectonicHazard()));
        harness.setGraveyard(player2, List.of(
                new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Descend is checked again when the attack trigger resolves")
    void descendMustStillBeMetOnResolution() {
        addReadyVeteran();
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(
                new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new ArmoredKincaller(), new ArmoredKincaller(), new ArmoredKincaller()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Descend counts lands and allows the attacking Veteran to target itself")
    void landsEnableDescendAndVeteranCanTargetItself() {
        Permanent veteran = addReadyVeteran();
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, veteran.getId());
        harness.passBothPriorities();

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger can put its counter on an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent veteran = addReadyVeteran();
        Permanent target = addCreatureReady(player2, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The attack trigger resolves even after the Veteran leaves the battlefield")
    void removingSourceDoesNotStopTrigger() {
        Permanent veteran = addReadyVeteran();
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(veteran);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only attacking with the Veteran triggers its descend ability")
    void anotherCreatureAttackingDoesNotTriggerVeteran() {
        Permanent veteran = addReadyVeteran();
        Permanent attacker = addCreatureReady(player1, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyVeteran() {
        return addCreatureReady(player1, new MalametVeteran());
    }
}
