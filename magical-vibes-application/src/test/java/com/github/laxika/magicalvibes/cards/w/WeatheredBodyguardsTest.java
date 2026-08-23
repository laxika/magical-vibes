package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeatheredBodyguards.class, GrizzlyBears.class})
class WeatheredBodyguardsTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Bodyguards redirect combat damage from unblocked creatures")
    void untappedBodyguardsRedirectDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        addUnblockedAttacker(player1);

        resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguards.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapped Bodyguards do not redirect combat damage")
    void tappedBodyguardsDoNotRedirectDamage() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        bodyguards.tap();
        addUnblockedAttacker(player1);

        resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Blocked creature combat damage is not redirected")
    void blockedDamageIsNotRedirected() {
        Permanent bodyguards = harness.addToBattlefieldAndReturn(player2, new WeatheredBodyguards());
        addUnblockedAttacker(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bodyguards.getMarkedDamage()).isZero();
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addUnblockedAttacker(Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }
}
