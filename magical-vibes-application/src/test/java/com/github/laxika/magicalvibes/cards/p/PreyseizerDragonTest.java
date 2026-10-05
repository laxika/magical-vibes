package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PreyseizerDragon.class, GrizzlyBears.class, LlanowarElves.class})
class PreyseizerDragonTest extends BaseCardTest {

    private void castDragon() {
        harness.castFromHand(player1, new PreyseizerDragon(), "{4}{R}{R}");
    }

    private Permanent dragon() {
        return findPermanent(player1, "Preyseizer Dragon");
    }

    @Test
    @DisplayName("Devour 2 gives twice the number of sacrificed creatures in +1/+1 counters")
    void devourAddsTwiceTheSacrificedCreatures() {
        Permanent fodder1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodder2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDragon();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder1.getId(), fodder2.getId()));

        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking deals damage equal to its +1/+1 counters to a creature")
    void attackingDealsCounterDamageToCreature() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castDragon();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player1, "Grizzly Bears")));
        dragon().setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
    @Test
    void mayDeclineDevourWithCreaturesAvailable() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PreyseizerDragon());
        castDragon();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(fodder);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void entersWithoutOtherCreaturesAndAttackDealsNoDamage() {
        castDragon();
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(dragon().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        dragon().setSummoningSick(false);
        harness.setLife(player2, 20);
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    void attackCountsCurrentPlusOneCountersAtResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PreyseizerDragon());
        attacker.setSummoningSick(false);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setCounterCount(CounterType.CHARGE, 7);
        harness.setLife(player2, 20);
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }
}
