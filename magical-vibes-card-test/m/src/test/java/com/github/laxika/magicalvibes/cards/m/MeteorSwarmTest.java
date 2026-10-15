package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
import com.github.laxika.magicalvibes.cards.z.ZarielArchdukeOfAvernus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeteorSwarm.class, DireWolfProwler.class, ZarielArchdukeOfAvernus.class, YouComeToARiver.class})
class MeteorSwarmTest extends BaseCardTest {

    @Test
    void dividesEightDamageAmongExactlyXCreaturesAndPlaneswalkers() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent zariel = harness.addToBattlefieldAndReturn(player2, new ZarielArchdukeOfAvernus());
        zariel.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorceryForX(player1, 0, 2, Map.of(
                wolf.getId(), 4,
                zariel.getId(), 4
        ));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dire Wolf Prowler");
        assertThat(zariel.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void requiresExactlyXTargets() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 2, Map.of(wolf.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 1, Map.of(player2.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastWithZeroTargetsForThreeRedMana() {
        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorceryForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Meteor Swarm");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void requiresDamageAssignmentsToTotalEight() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 1, Map.of(wolf.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eachTargetMustReceiveAtLeastOneDamage() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent zariel = harness.addToBattlefieldAndReturn(player2, new ZarielArchdukeOfAvernus());
        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 2, Map.of(wolf.getId(), 0, zariel.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeDamageFromAnIllegalTarget() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent zariel = harness.addToBattlefieldAndReturn(player2, new ZarielArchdukeOfAvernus());
        zariel.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorceryForX(player1, 0, 2, Map.of(wolf.getId(), 4, zariel.getId(), 4));
        harness.setHand(player2, List.of(new YouComeToARiver()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castModalInstant(player2, 0, 0, List.of(wolf.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Zariel, Archduke of Avernus");
        assertThat(zariel.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }
}
