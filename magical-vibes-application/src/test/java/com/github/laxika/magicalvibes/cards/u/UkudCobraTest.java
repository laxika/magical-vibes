package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.SegmentedKrotiq;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UkudCobra.class, SegmentedKrotiq.class})
class UkudCobraTest extends BaseCardTest {

    @Test
    @DisplayName("Ukud Cobra destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent cobra = harness.addToBattlefieldAndReturn(player1, new UkudCobra());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SegmentedKrotiq());

        cobra.setSummoningSick(false);
        cobra.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(cobra.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Ukud Cobra destroys a larger attacker when blocking")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = addCreatureReady(player1, new SegmentedKrotiq());
        Permanent cobra = harness.addToBattlefieldAndReturn(player2, new UkudCobra());
        attacker.setAttacking(true);
        cobra.setBlocking(true);
        cobra.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(cobra.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An unblocked Ukud Cobra deals normal damage to a player")
    void deathtouchDoesNotMakePlayerDamageLethal() {
        Permanent cobra = addCreatureReady(player1, new UkudCobra());
        cobra.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cobra);
    }
}
