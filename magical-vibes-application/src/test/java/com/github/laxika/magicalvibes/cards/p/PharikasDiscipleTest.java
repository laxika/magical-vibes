package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PharikasDisciple.class, GiantSpider.class})
class PharikasDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent disciple = addCreatureReady(player1, new PharikasDisciple());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(disciple.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent disciple = addCreatureReady(player1, new PharikasDisciple());
        disciple.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker that survives the raw damage, and renown does not trigger")
    void deathtouchKillsBlockerWithoutRenown() {
        Permanent disciple = addCreatureReady(player1, new PharikasDisciple());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(disciple.isRenowned()).isFalse();
    }

    @Test
    @DisplayName("Deathtouch also destroys an attacker when the Disciple blocks, without becoming renowned")
    void deathtouchWorksWhileBlocking() {
        addCreatureReady(player1, new GiantSpider());
        Permanent disciple = addCreatureReady(player2, new PharikasDisciple());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Pharika's Disciple");
        harness.assertLife(player2, 20);
        assertThat(disciple.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(disciple.isRenowned()).isFalse();
    }
}
