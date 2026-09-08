package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutOfTheWay.class, AirElemental.class, BirdsOfParadise.class, Island.class})
class OutOfTheWayTest extends BaseCardTest {

    @Test
    void returnsGreenOpponentPermanentForReducedCostAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
        harness.setHand(player1, List.of(new OutOfTheWay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Birds of Paradise");
        harness.assertInHand(player2, "Birds of Paradise");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceCostForNonGreenPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new OutOfTheWay()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOwnGreenPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
        harness.setHand(player1, List.of(new OutOfTheWay()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent an opponent controls");
    }

    @Test
    void cannotTargetOpponentLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new OutOfTheWay()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent an opponent controls");
    }
}
