package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinTownFlunkies.class, GrizzlyBears.class})
class GoblinTownFlunkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield amasses Goblins 1")
    void amassesGoblinsOnEnter() {
        harness.setHand(player1, List.of(new GoblinTownFlunkies()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
    }

    @Test
    @DisplayName("Entering the battlefield amasses on an existing Army")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new GoblinTownFlunkies()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Flunkies can attack immediately, but its new Army cannot")
    void hasteDoesNotExtendToArmy() {
        harness.setHand(player1, List.of(new GoblinTownFlunkies()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, findPermanent(player1, "Goblin-town Flunkies"), player1.getId())).isTrue();
        assertThat(als.canAttack(gd, findPermanent(player1, "Goblin Army"), player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A second Flunkies grows the same Army instead of creating another")
    void repeatedAmassGrowsExistingToken() {
        harness.setHand(player1, List.of(new GoblinTownFlunkies(), new GoblinTownFlunkies()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent army = findPermanent(player1, "Goblin Army");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opposing Army does not prevent creating your own Army")
    void ignoresOpponentsArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GoblinTownFlunkies());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new GoblinTownFlunkies()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Goblin Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With multiple Armies only the chosen Army receives the counter")
    void choosesOneOfMultipleArmies() {
        Permanent firstArmy = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        Permanent secondArmy = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());
        firstArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        secondArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new GoblinTownFlunkies()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondArmy.getId()));

        assertThat(firstArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }
}
