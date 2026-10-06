package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OracleOfNectars;
import com.github.laxika.magicalvibes.cards.p.PowerOfFire;
import com.github.laxika.magicalvibes.cards.t.ThornwatchScarecrow;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SickleRipper.class, ThornwatchScarecrow.class, OracleOfNectars.class, PowerOfFire.class})
class SickleRipperTest extends BaseCardTest {

    @Test
    @DisplayName("Wither deals combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusCountersToBlocker() {
        // Thornwatch Scarecrow is a 4/4 blocker, so it survives and we can inspect the counters.
        addCreatureReady(player2, new ThornwatchScarecrow());

        addCreatureReady(player1, new SickleRipper());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        // Sickle Ripper's 2 damage is dealt as counters rather than marked damage.
        Permanent survivor = findPermanent(player2, "Thornwatch Scarecrow");
        assertThat(survivor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(survivor.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Wither combat damage of lethal counters kills the blocker")
    void witherKillsSmallBlocker() {
        addCreatureReady(player1, new SickleRipper());

        addCreatureReady(player2, new OracleOfNectars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        // Two -1/-1 counters make the 2/2 a 0/0; it dies.
        harness.assertInGraveyard(player2, "Oracle of Nectars");
    }

    @Test
    @DisplayName("Wither deals normal life loss to a player, not -1/-1 counters")
    void witherDealsNormalDamageToPlayer() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SickleRipper());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Wither deals blocking damage as counters even when Sickle Ripper dies")
    void witherDealsCountersWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new ThornwatchScarecrow());
        addCreatureReady(player2, new SickleRipper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Thornwatch Scarecrow");
        harness.assertInGraveyard(player2, "Sickle Ripper");
    }

    @Test
    @DisplayName("Wither applies to noncombat damage from a granted ability")
    void witherAppliesToGrantedDamageAbility() {
        Permanent ripper = addCreatureReady(player1, new SickleRipper());
        Permanent target = addCreatureReady(player2, new ThornwatchScarecrow());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(ripper.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Thornwatch Scarecrow");
    }
}
