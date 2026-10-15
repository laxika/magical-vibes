package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistyMountainsRaider.class, GrizzlyBears.class})
class MistyMountainsRaiderTest extends BaseCardTest {

    @Test
    void attackingAmassesTwoGoblinsAndCreatesAnArmyWhenNeeded() {
        addCreatureReady(player1, new MistyMountainsRaider());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
    }

    @Test
    void attackingAddsTwoCountersAndGoblinSubtypeToAnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        addCreatureReady(player1, new MistyMountainsRaider());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
    }

    @Test
    void attackingWithAnotherCreatureTriggersEachNonattackingRaiderOnce() {
        harness.addToBattlefield(player1, new MistyMountainsRaider());
        harness.addToBattlefield(player1, new MistyMountainsRaider());
        addCreatureReady(player1, new MistyMountainsRaider());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Army")).hasSize(1);
        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void multipleAttackersTriggerEachRaiderOnlyOnce() {
        addCreatureReady(player1, new MistyMountainsRaider());
        addCreatureReady(player1, new MistyMountainsRaider());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Army")).hasSize(1);
        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void opponentsAttackDoesNotTriggerYourRaider() {
        harness.addToBattlefield(player1, new MistyMountainsRaider());
        addCreatureReady(player2, new MistyMountainsRaider());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(findPermanent(player2, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
