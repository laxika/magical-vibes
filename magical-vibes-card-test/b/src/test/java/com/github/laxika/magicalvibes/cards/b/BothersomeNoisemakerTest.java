package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BothersomeNoisemaker.class, GrizzlyBears.class, Shock.class})
class BothersomeNoisemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell amasses Goblins 1")
    void noncreatureSpellAmassesGoblins() {
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
    }

    @Test
    @DisplayName("Casting a creature spell does not amass Goblins")
    void creatureSpellDoesNotAmassGoblins() {
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }

    @Test
    @DisplayName("Amassing uses an existing Army")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The amass trigger resolves before the noncreature spell")
    void amassResolvesBeforeSpell() {
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger amass")
    void opponentSpellDoesNotAmass() {
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(findPermanents(player2, "Goblin Army")).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each noncreature spell grows the same Army")
    void repeatedSpellsGrowSameArmy() {
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        Permanent army = findPermanent(player1, "Goblin Army");
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin Army")).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Amass lets its controller choose one of multiple Armies")
    void choosesOneArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.addToBattlefield(player1, new BothersomeNoisemaker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.GOBLIN);
        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
    }
}
