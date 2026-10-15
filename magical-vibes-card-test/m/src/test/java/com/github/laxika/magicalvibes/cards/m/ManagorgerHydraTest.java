package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({ManagorgerHydra.class, GrizzlyBears.class, Ornithopter.class, Disperse.class})
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
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting the Hydra does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.setHand(player1, List.of(new ManagorgerHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(getHydra().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A noncreature spell adds its counter before the spell resolves")
    void noncreatureSpellTriggerResolvesFirst() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent hydra = getHydra();

        harness.castInstant(player1, 0, hydra.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Managorger Hydra");

        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Managorger Hydra");
        harness.assertInHand(player1, "Managorger Hydra");
    }

    @Test
    @DisplayName("A pending trigger cannot put counters on a Hydra that has left the battlefield")
    void removalInResponseLeavesOriginalTriggerWithoutSource() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        Permanent hydra = getHydra();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hydra.getId());

        harness.passBothPriorities();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Managorger Hydra");
        harness.assertInHand(player1, "Managorger Hydra");

        resolveAllTriggers();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each player's Hydra gets a counter from the same spell")
    void hydrasOnBothSidesTriggerIndependently() {
        harness.addToBattlefield(player1, new ManagorgerHydra());
        harness.addToBattlefield(player2, new ManagorgerHydra());
        harness.setHand(player1, List.of(new Ornithopter()));
        Permanent firstHydra = getHydra();
        Permanent secondHydra = findPermanent(player2, "Managorger Hydra");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(firstHydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondHydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent getHydra() {
        return findPermanent(player1, "Managorger Hydra");
    }
}
