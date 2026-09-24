package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlithPredator.class, OmegaMyr.class, GraniteShard.class})
class SlithPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamageToPlayer() {
        Permanent predator = addCreatureReady(player1, new SlithPredator());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when blocked without dealing damage to a player")
    void noCounterWhenBlocked() {
        Permanent predator = addCreatureReady(player1, new SlithPredator());
        Permanent blocker = addCreatureReady(player2, new OmegaMyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not get a counter from noncombat damage")
    void noCounterOnNoncombatDamage() {
        Permanent predator = addCreatureReady(player1, new SlithPredator());
        Permanent graniteShard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(graniteShard),
                null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
