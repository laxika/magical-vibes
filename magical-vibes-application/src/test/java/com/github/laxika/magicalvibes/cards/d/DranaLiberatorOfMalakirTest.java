package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DranaLiberatorOfMalakir.class, AirElemental.class, GrizzlyBears.class})
class DranaLiberatorOfMalakirTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each attacking creature you control")
    void putsCountersOnEachAttackingCreatureYouControl() {
        Permanent drana = addCreatureReady(player1, new DranaLiberatorOfMalakir());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(drana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when Drana deals no combat damage to a player")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent drana = addCreatureReady(player1, new DranaLiberatorOfMalakir());
        addCreatureReady(player2, new AirElemental());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(drana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
