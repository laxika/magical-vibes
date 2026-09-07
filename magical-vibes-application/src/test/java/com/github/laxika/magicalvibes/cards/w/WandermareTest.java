package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RimrockKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wandermare.class, RimrockKnight.class, GrizzlyBears.class})
class WandermareTest extends BaseCardTest {

    @Test
    void getsACounterWhenCreatureFaceOfAdventureCardIsCast() {
        Permanent wandermare = addCreatureReady(player1, new Wandermare());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNonAdventureCreaturesOrAdventureFaces() {
        Permanent wandermare = addCreatureReady(player1, new Wandermare());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
