package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OutlandColossus;
import com.github.laxika.magicalvibes.cards.v.ValorsFlagship;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderousVelocipedeTest extends BaseCardTest {

    @Test
    void givesOneCounterToCreaturesAndVehiclesWithManaValueAtMostFour() {
        harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());

        Permanent creature = cast(player1, new GrizzlyBears(), ManaColor.GREEN, 1, 1);
        Permanent vehicle = cast(player1, new AirResponseUnit(), ManaColor.WHITE, 1, 2);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void givesThreeCountersToCreaturesAndVehiclesWithManaValueAboveFour() {
        harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());

        Permanent creature = cast(player1, new OutlandColossus(), ManaColor.GREEN, 2, 3);
        Permanent vehicle = cast(player1, new ValorsFlagship(), ManaColor.WHITE, 3, 4);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotAffectOtherPlayersOrNonCreatureNonVehicles() {
        harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());

        harness.forceActivePlayer(player2);
        Permanent opponentCreature = cast(player2, new GrizzlyBears(), ManaColor.GREEN, 1, 1);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Forest");

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent cast(Player player, Card card,
                           ManaColor coloredMana, int coloredAmount, int colorlessAmount) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, coloredMana, coloredAmount);
        harness.addMana(player, ManaColor.COLORLESS, colorlessAmount);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, card.getName());
    }
}
