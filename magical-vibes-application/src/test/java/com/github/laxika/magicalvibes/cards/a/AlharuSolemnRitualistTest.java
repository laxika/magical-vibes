package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlharuSolemnRitualist.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class AlharuSolemnRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on each of up to two other creatures")
    void etbPutsCountersOnTwoOtherCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAlharu(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken creature with a +1/+1 counter dying creates a Spirit")
    void counteredAllyDeathCreatesSpirit() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        killWithShock(elves);

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter dying creates no Spirit")
    void uncounteredAllyDeathCreatesNoSpirit() {
        harness.addToBattlefield(player1, new AlharuSolemnRitualist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        killWithShock(elves);

        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Alharu creates a Spirit when it dies with a +1/+1 counter")
    void selfDeathCreatesSpirit() {
        Permanent alharu = harness.addToBattlefieldAndReturn(player1, new AlharuSolemnRitualist());
        alharu.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        alharu.setMarkedDamage(4);

        harness.runStateBasedActions();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    private void castAlharu(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AlharuSolemnRitualist()));
        addAlharuMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addAlharuMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
