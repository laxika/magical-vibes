package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Oversimplify.class, AirElemental.class, GrizzlyBears.class, Island.class})
class OversimplifyTest extends BaseCardTest {

    @Test
    void exilesCreaturesAndCreatesPerPlayerFractalsWithTheirTotalPower() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());

        cast();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");

        Permanent playerOneFractal = findPermanent(player1, "Fractal");
        Permanent playerTwoFractal = findPermanent(player2, "Fractal");
        assertThat(playerOneFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(playerTwoFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(playerOneFractal.getEffectivePower()).isEqualTo(6);
        assertThat(playerTwoFractal.getEffectivePower()).isEqualTo(2);
    }

    private void cast() {
        harness.setHand(player1, List.of(new Oversimplify()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
