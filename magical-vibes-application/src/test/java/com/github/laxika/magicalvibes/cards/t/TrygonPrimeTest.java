package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GreyKnightParagon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrygonPrime.class, GreyKnightParagon.class})
class TrygonPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grows Trygon Prime and another attacker, making the other attacker unblockable")
    void attacksGrowAndMakeAnotherAttackerUnblockable() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());
        Permanent otherAttacker = addCreatureReady(player1, new GreyKnightParagon());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(trygon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The other attacking creature target is optional")
    void otherAttackerTargetIsOptional() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());
        Permanent otherAttacker = addCreatureReady(player1, new GreyKnightParagon());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(trygon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherAttacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Only another attacking creature is a legal target")
    void rejectsIllegalTargets() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());
        Permanent nonAttacker = addCreatureReady(player1, new GreyKnightParagon());
        Permanent legalAttacker = addCreatureReady(player1, new GreyKnightParagon());

        declareAttackers(player1, List.of(0, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, trygon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, legalAttacker.getId());
        harness.passBothPriorities();
        assertThat(legalAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking alone still puts a counter on Trygon Prime")
    void attackingAloneStillGrows() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(trygon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(trygon.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The whole trigger fails to resolve if its chosen target stops attacking")
    void targetMustStillBeAttackingOnResolution() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());
        Permanent otherAttacker = addCreatureReady(player1, new GreyKnightParagon());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        otherAttacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(trygon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherAttacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Losing the chosen target prevents the counter on Trygon Prime too")
    void removedTargetPreventsAllEffects() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());
        Permanent otherAttacker = addCreatureReady(player1, new GreyKnightParagon());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(otherAttacker);
        gd.playerGraveyards.get(player1.getId()).add(otherAttacker.getCard());
        harness.passBothPriorities();

        assertThat(trygon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The other attacker still benefits if Trygon Prime leaves before resolution")
    void sourceLeavingDoesNotPreventTargetEffects() {
        Permanent trygon = addCreatureReady(player1, new TrygonPrime());
        Permanent otherAttacker = addCreatureReady(player1, new GreyKnightParagon());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trygon);
        gd.playerGraveyards.get(player1.getId()).add(trygon.getCard());
        harness.passBothPriorities();

        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.isCantBeBlocked()).isTrue();
    }
}