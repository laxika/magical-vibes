package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.cards.r.RimrockKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wandermare.class, RimrockKnight.class, SporecapSpider.class})
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
        harness.setHand(player1, List.of(new SporecapSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent target = addCreatureReady(player1, new SporecapSpider());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForAnOpponentsAdventureCreature() {
        Permanent wandermare = addCreatureReady(player1, new Wandermare());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RimrockKnight()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Rimrock Knight");
    }

    @Test
    void triggersForEachAdventureCreatureAndEachControlledWandermare() {
        Permanent first = addCreatureReady(player1, new Wandermare());
        Permanent second = addCreatureReady(player1, new Wandermare());
        harness.setHand(player1, List.of(new RimrockKnight(), new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersWhenCreatureIsCastFromExileAfterItsAdventure() {
        Permanent wandermare = addCreatureReady(player1, new Wandermare());
        RimrockKnight knight = new RimrockKnight();
        harness.setHand(player1, List.of(knight));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAdventure(player1, 0, wandermare.getId());
        resolveAllTriggers();
        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castFromExile(player1, knight.getId());
        harness.passBothPriorities();

        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Rimrock Knight");

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Rimrock Knight");
        assertThat(wandermare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
