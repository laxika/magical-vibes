package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cloudthresher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SonicTheHedgehog.class, Cloudthresher.class, GrizzlyBears.class, Shock.class})
class SonicTheHedgehogTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts counters on controlled creatures with flash or haste")
    void attackCountersFlashOrHasteCreatures() {
        Permanent sonic = addCreatureReady(player1, new SonicTheHedgehog());
        Permanent flashCreature = addCreatureReady(player1, new Cloudthresher());
        Permanent vanillaCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(sonic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flashCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vanillaCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage to a controlled creature with flash or haste creates a tapped Treasure")
    void damageToFlashCreatureCreatesTappedTreasure() {
        harness.addToBattlefield(player1, new SonicTheHedgehog());
        Permanent flashCreature = addCreatureReady(player1, new Cloudthresher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID flashCreatureId = flashCreature.getId();
        harness.castInstant(player2, 0, flashCreatureId);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Damage to a controlled creature without flash or haste does not create a Treasure")
    void damageToCreatureWithoutFlashOrHasteDoesNotCreateTreasure() {
        harness.addToBattlefield(player1, new SonicTheHedgehog());
        Permanent vanillaCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, vanillaCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
