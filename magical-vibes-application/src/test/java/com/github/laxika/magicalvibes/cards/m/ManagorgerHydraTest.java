package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManagorgerHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a spell puts a +1/+1 counter on the Hydra")
    void controllerSpellAddsCounter() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent hydra = getHydra();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, hydra)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, hydra)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a spell also puts a +1/+1 counter on the Hydra")
    void opponentSpellAddsCounter() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        Permanent hydra = getHydra();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless spell triggers the Hydra too")
    void colorlessSpellAddsCounter() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.setHand(player1, List.of(new Ornithopter()));

        Permanent hydra = getHydra();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters accumulate across multiple spells")
    void countersAccumulate() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent hydra = getHydra();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent getHydra() {
        return findPermanent(player1, "Managorger Hydra");
    }
}
