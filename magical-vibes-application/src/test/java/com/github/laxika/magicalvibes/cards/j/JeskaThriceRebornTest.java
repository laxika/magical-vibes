package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeskaThriceReborn.class, GrizzlyBears.class})
class JeskaThriceRebornTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one loyalty counter for each commander cast from the command zone")
    void entersWithCommanderCastCounters() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());

        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());

        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zero ability triples the targeted creature's combat damage to an opponent")
    void triplesTargetedCreatureCombatDamage() {
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 0,
                null, bear.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    @Test
    @DisplayName("Minus X ability deals X damage to up to three targets")
    void dealsDamageToThreeTargets() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jeska = harness.enterBattlefieldAndReturn(player1, new JeskaThriceReborn());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jeska), 1, 1,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(third.getMarkedDamage()).isEqualTo(1);
        assertThat(jeska.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }
}
