package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreatForestDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarknessDescends.class, GrizzlyBears.class, GreatForestDruid.class, Forest.class})
class DarknessDescendsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two -1/-1 counters on each creature controlled by either player")
    void putsTwoMinusOneMinusOneCountersOnEachCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, largeCreature());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, largeCreature());

        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two counters cause a 2/2 creature to die")
    void twoCountersKillTwoTwoCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature permanents receive no counters")
    void leavesNoncreaturePermanentsUntouched() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(land.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opponentLand.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Counters accumulate and cancel existing +1/+1 counters")
    void handlesExistingCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreatForestDruid());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreatForestDruid());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can resolve when neither player controls a creature")
    void resolvesWithoutCreatures() {
        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Darkness Descends");
        assertThat(gd.stack).isEmpty();
    }

    private static Card largeCreature() {
        Card card = new Card();
        card.setName("Large Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(4);
        card.setToughness(5);
        return card;
    }
}
