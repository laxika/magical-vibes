package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LivingLands;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterBiomancer.class, GrizzlyBears.class, Forest.class, LivingLands.class, Solemnity.class})
class MasterBiomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Other creature you control enters with +1/+1 counters equal to Master Biomancer's power and as a Mutant")
    void otherCreatureEntersWithCountersAndMutantType() {
        addReadyBiomancer(player1);

        Permanent bears = castBears(player1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Counter count follows Master Biomancer's current power, not its printed power")
    void countersScaleWithCurrentPower() {
        Permanent biomancer = addReadyBiomancer(player1);
        biomancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        Permanent bears = castBears(player1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Two Master Biomancers stack their counter grants")
    void twoBiomancersStack() {
        addReadyBiomancer(player1);
        addReadyBiomancer(player1);

        Permanent bears = castBears(player1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's creature is unaffected")
    void opponentCreatureUnaffected() {
        addReadyBiomancer(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedSubtypes()).doesNotContain(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Master Biomancer itself enters without counters when no other one is on the battlefield")
    void biomancerDoesNotAffectItself() {
        harness.castFromHand(player1, new MasterBiomancer(), "{2}{G}{U}");
        harness.passBothPriorities();

        Permanent biomancer = findPermanent(player1, "Master Biomancer");
        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(biomancer.getGrantedSubtypes()).doesNotContain(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("A creature that entered as a Mutant stays a Mutant after Master Biomancer leaves")
    void mutantTypePersistsAfterBiomancerLeaves() {
        Permanent biomancer = addReadyBiomancer(player1);

        Permanent bears = castBears(player1);
        gd.playerBattlefields.get(player1.getId()).remove(biomancer);

        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void zeroPowerStillGrantsMutantType() {
        Permanent biomancer = addReadyBiomancer(player1);
        biomancer.setPowerModifier(-2);

        Permanent bears = castBears(player1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    void negativePowerDoesNotPreventMutantType() {
        Permanent biomancer = addReadyBiomancer(player1);
        biomancer.setPowerModifier(-3);

        Permanent bears = castBears(player1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    void anotherBiomancerEntersWithCountersAndMutantType() {
        addReadyBiomancer(player1);

        Permanent second = harness.enterBattlefieldAndReturn(player1, new MasterBiomancer());

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
        Permanent bears = castBears(player1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void counterProhibitionDoesNotPreventMutantType() {
        addReadyBiomancer(player1);
        harness.addToBattlefield(player2, new Solemnity());

        Permanent bears = castBears(player1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    void forestEnteringAsCreatureGetsCountersAndMutantType() {
        addReadyBiomancer(player1);
        harness.addToBattlefield(player1, new LivingLands());

        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    private Permanent castBears(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player, "Grizzly Bears");
    }

    private Permanent addReadyBiomancer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MasterBiomancer());
        perm.setSummoningSick(false);
        return perm;
    }
}
