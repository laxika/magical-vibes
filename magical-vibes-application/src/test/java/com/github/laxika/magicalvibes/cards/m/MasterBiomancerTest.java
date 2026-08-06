package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getGrantedSubtypes()).doesNotContain(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Master Biomancer itself enters without counters when no other one is on the battlefield")
    void biomancerDoesNotAffectItself() {
        harness.setHand(player1, List.of(new MasterBiomancer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
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

    private Permanent castBears(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "Grizzly Bears");
    }

    private Permanent addReadyBiomancer(Player player) {
        Permanent perm = new Permanent(new MasterBiomancer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
