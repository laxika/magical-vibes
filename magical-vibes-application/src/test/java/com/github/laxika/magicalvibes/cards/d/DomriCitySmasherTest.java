package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DomriCitySmasher.class, GrizzlyBears.class})
class DomriCitySmasherTest extends BaseCardTest {

    @Test
    @DisplayName("+2 boosts and grants haste to your creatures until end of turn")
    void plusTwoBoostsOwnCreaturesAndGrantsHaste() {
        addReadyDomri(player1, 5);
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("-3 deals 3 damage to any target")
    void minusThreeDealsDamageToCreature() {
        Permanent domri = addReadyDomri(player1, 5);
        Permanent creature = addCreature(player2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("-8 puts three +1/+1 counters on your creatures and grants trample until end of turn")
    void minusEightPutsCountersAndGrantsTrample() {
        Permanent domri = addReadyDomri(player1, 9);
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(5);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyDomri(Player player, int loyalty) {
        Permanent domri = new Permanent(new DomriCitySmasher());
        domri.setCounterCount(CounterType.LOYALTY, loyalty);
        domri.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(domri);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return domri;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
