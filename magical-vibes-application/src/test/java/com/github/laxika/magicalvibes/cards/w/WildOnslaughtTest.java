package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({WildOnslaught.class, BalothGorger.class, ShortSword.class})
class WildOnslaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker — puts one +1/+1 counter on each creature you control")
    void withoutKickerPutsOneCounter() {
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player1, new BalothGorger());
        harness.castFromHand(player1, new WildOnslaught(), "{3}{G}");
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player1, "Baloth Gorger");

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("With kicker — puts two +1/+1 counters on each creature you control instead")
    void withKickerPutsTwoCounters() {
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player1, new BalothGorger());
        harness.setHand(player1, List.of(new WildOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player1, "Baloth Gorger");

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Does not put counters on opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.castFromHand(player1, new WildOnslaught(), "{3}{G}");
        harness.passBothPriorities();

        Permanent ownBear = findPermanent(player1, "Baloth Gorger");
        Permanent opponentBear = findPermanent(player2, "Baloth Gorger");

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolves without error when no creatures are on the battlefield")
    void resolvesWithNoCreatures() {
        harness.castFromHand(player1, new WildOnslaught(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kicked spell affects only controlled creatures, not artifacts or opposing creatures")
    void kickedSpellExcludesNoncreaturesAndOpponents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        harness.setHand(player1, List.of(new WildOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters are added to creatures present at resolution, not creatures entering afterward")
    void affectsCreaturesPresentAtResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        existing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ShortSword());
        harness.castFromHand(player1, new WildOnslaught(), "{3}{G}");
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(beforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(afterResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
