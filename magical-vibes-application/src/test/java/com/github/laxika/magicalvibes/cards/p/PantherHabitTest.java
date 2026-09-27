package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PantherHabit.class, GrizzlyBears.class, Shock.class})
class PantherHabitTest extends BaseCardTest {

    @Test
    void preventsDamageToEquippedCreatureAndAddsCounters() {
        Permanent creature = addCreature(player2);
        Permanent equipment = new Permanent(new PantherHabit());
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        castShockAt(creature);

        assertThat(findPermanent(player2, "Grizzly Bears")).isSameAs(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPreventDamageWhenUnattached() {
        Permanent creature = addCreature(player2);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new PantherHabit()));

        castShockAt(creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void castShockAt(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
