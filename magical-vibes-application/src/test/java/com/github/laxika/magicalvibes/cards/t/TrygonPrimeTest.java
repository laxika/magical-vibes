package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrygonPrime.class, GrizzlyBears.class})
class TrygonPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grows Trygon Prime and another attacker, making the other attacker unblockable")
    void attacksGrowAndMakeAnotherAttackerUnblockable() {
        Permanent trygon = addReadyCreature(player1, new TrygonPrime());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());

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
        Permanent trygon = addReadyCreature(player1, new TrygonPrime());
        Permanent otherAttacker = addReadyCreature(player1, new GrizzlyBears());

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
        Permanent trygon = addReadyCreature(player1, new TrygonPrime());
        Permanent nonAttacker = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, trygon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
