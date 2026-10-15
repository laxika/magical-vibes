package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgeConductor.class, Ornithopter.class, GrizzlyBears.class})
class SurgeConductorTest extends BaseCardTest {

    @Test
    void nontokenArtifactEnteringUnderYourControlTriggersProliferate() {
        harness.addToBattlefield(player1, new SurgeConductor());
        Permanent bears = addCounteredBears(player1);

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentArtifactEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SurgeConductor());
        Permanent bears = addCounteredBears(player1);

        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonartifactEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SurgeConductor());
        Permanent bears = addCounteredBears(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void proliferateCanChooseNoPermanents() {
        harness.addToBattlefield(player1, new SurgeConductor());
        Permanent bears = addCounteredBears(player1);

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addCounteredBears(Player player) {
        Permanent bears = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bears;
    }
}
