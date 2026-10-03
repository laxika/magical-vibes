package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkroanSergeant.class, GiantSpider.class, TopanFreeblade.class})
class AkroanSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent sergeant = addCreatureReady(player1, new AkroanSergeant());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(sergeant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sergeant.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent sergeant = addCreatureReady(player1, new AkroanSergeant());
        sergeant.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(sergeant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Renown does not trigger when the creature is blocked")
    void noRenownWhenBlocked() {
        Permanent sergeant = addCreatureReady(player1, new AkroanSergeant());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(sergeant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sergeant.isRenowned()).isFalse();
    }

    @Test
    @DisplayName("An already-renowned creature does not create a renown trigger")
    void alreadyRenownedDoesNotTrigger() {
        Permanent sergeant = addCreatureReady(player1, new AkroanSergeant());
        sergeant.setRenowned(true);
        sergeant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        sergeant.setAttacking(true);
        prepareDeclareBlockers();

        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(sergeant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent sergeant = addCreatureReady(player1, new AkroanSergeant());
        addCreatureReady(player2, new TopanFreeblade());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Akroan Sergeant");
        harness.assertInGraveyard(player2, "Topan Freeblade");
        assertThat(sergeant.getMarkedDamage()).isZero();
        assertThat(sergeant.isRenowned()).isFalse();
        harness.assertLife(player2, 20);
    }
}
