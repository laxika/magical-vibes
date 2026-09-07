package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Borborygmos.class, ColossalDreadmaw.class, Forest.class, GrizzlyBears.class})
class BorborygmosTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature its controller controls after combat damage")
    void putsCountersOnControlledCreaturesAfterCombatDamage() {
        Permanent borborygmos = addCreatureReady(player1, new Borborygmos());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(borborygmos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Forest")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a blocker takes all combat damage")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent borborygmos = addCreatureReady(player1, new Borborygmos());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(borborygmos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
