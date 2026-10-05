package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RazorfieldRhino;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueMyr.class, RazorfieldRhino.class})
class PlagueMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Plague Myr produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent perm = addCreatureReady(player1, new PlagueMyr());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked Plague Myr deals 1 poison counter instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new PlagueMyr());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked Plague Myr deals -1/-1 counters to blocker instead of regular damage")
    void dealsMinusCountersToBlocker() {
        Permanent blocker = addCreatureReady(player2, new RazorfieldRhino());
        Permanent attacker = addCreatureReady(player1, new PlagueMyr());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plague Myr");
        harness.assertInGraveyard(player1, "Plague Myr");
        harness.assertOnBattlefield(player2, "Razorfield Rhino");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Summoning-sick Plague Myr cannot tap for mana")
    void summoningSicknessPreventsManaAbility() {
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new PlagueMyr());
        myr.setSummoningSick(true);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(myr.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Tapped Plague Myr cannot produce mana a second time")
    void cannotTapTwiceForMana() {
        addCreatureReady(player1, new PlagueMyr());
        gs.tapPermanent(gd, player1, 0);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking Plague Myr deals infect damage to the attacker")
    void blockingDealsMinusCountersToAttacker() {
        Permanent attacker = addCreatureReady(player1, new RazorfieldRhino());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new PlagueMyr());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Plague Myr");
        harness.assertNotOnBattlefield(player2, "Plague Myr");
        harness.assertOnBattlefield(player1, "Razorfield Rhino");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }
}
