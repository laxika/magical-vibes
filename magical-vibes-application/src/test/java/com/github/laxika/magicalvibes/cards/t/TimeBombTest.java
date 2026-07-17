package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeBombTest extends BaseCardTest {

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger puts a time counter on Time Bomb")
    void upkeepTriggerAddsTimeCounter() {
        Permanent bomb = addReadyBomb(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // upkeep trigger goes on stack
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(bomb.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    // ===== Activated ability: mass damage =====

    @Test
    @DisplayName("Sacrificing Time Bomb deals damage equal to time counters to each creature and each player")
    void sacrificeDealsDamageToEachCreatureAndPlayer() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.TIME, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 3 damage kills both 2/2 bears and hits both players
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player1, "Time Bomb");
        harness.assertInGraveyard(player1, "Time Bomb");
    }

    @Test
    @DisplayName("Sacrificing Time Bomb with 0 counters deals 0 damage")
    void sacrificeWithZeroCountersDealsZeroDamage() {
        addReadyBomb(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Time Bomb");
    }

    @Test
    @DisplayName("Time counters are snapshotted before sacrifice so damage is correct")
    void timeCountersSnapshotBeforeSacrifice() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.TIME, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Time Bomb is sacrificed immediately as cost
        harness.assertNotOnBattlefield(player1, "Time Bomb");

        harness.passBothPriorities();

        // Damage should still equal 5 even though Time Bomb is gone
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Activated ability requires tap — cannot activate when tapped")
    void activatedAbilityRequiresTap() {
        Permanent bomb = addReadyBomb(player1);
        bomb.setCounterCount(CounterType.TIME, 3);
        bomb.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private Permanent addReadyBomb(Player player) {
        TimeBomb card = new TimeBomb();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
