package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifecraftCavalry.class, DruidOfTheCowl.class})
class LifecraftCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters after your permanent leaves the battlefield")
    void entersWithCountersAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castCavalry();

        Permanent cavalry = findCavalry();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when no permanent left the battlefield")
    void entersWithoutCountersWithoutRevolt() {
        castCavalry();

        Permanent cavalry = findCavalry();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy revolt")
    void opponentPermanentLeavingDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castCavalry();

        Permanent cavalry = findCavalry();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Revolt is checked on entry even when the permanent leaves after casting")
    void permanentLeavingWhileSpellIsOnStackSatisfiesRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.setHand(player1, List.of(new LifecraftCavalry()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passBothPriorities();

        assertThat(findCavalry().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple permanents leaving still add only two counters")
    void multipleDeparturesDoNotIncreaseCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });

        castCavalry();

        assertThat(findCavalry().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A permanent going to exile satisfies revolt")
    void exileSatisfiesRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));

        castCavalry();

        assertThat(findCavalry().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castCavalry() {
        harness.setHand(player1, List.of(new LifecraftCavalry()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findCavalry() {
        return findPermanent(player1, "Lifecraft Cavalry");
    }
}
