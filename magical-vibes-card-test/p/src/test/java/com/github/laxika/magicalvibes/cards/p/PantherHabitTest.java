package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PantherHabit.class, GrizzlyBears.class, Shock.class, Skullcrack.class})
class PantherHabitTest extends BaseCardTest {

    @Test
    void preventsDamageToEquippedCreatureAndAddsCounters() {
        Permanent creature = addCreature(player2);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PantherHabit());
        equipment.setAttachedTo(creature.getId());

        castShockAt(creature);

        assertThat(findPermanent(player2, "Grizzly Bears")).isSameAs(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPreventDamageWhenUnattached() {
        Permanent creature = addCreature(player2);
        harness.addToBattlefield(player2, new PantherHabit());

        castShockAt(creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    void unpreventableDamageStillAddsCountersButIsDealt() {
        Permanent creature = addCreature(player2);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PantherHabit());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        castShockAt(creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void equipAttachesAndCanMoveToAnotherCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new PantherHabit());
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(first.getId());

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
    }

    @Test
    void repeatedDamageAddsCountersForEachEvent() {
        Permanent creature = addCreature(player2);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PantherHabit());
        equipment.setAttachedTo(creature.getId());

        castShockAt(creature);
        castShockAt(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castShockAt(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
