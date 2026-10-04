package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.s.Staggershock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmlessAssault.class, NestInvader.class, Staggershock.class})
class HarmlessAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage dealt by an attacking creature")
    void preventsCombatDamageFromAttacker() {
        harness.setLife(player2, 20);
        castAndResolve();

        Permanent attacker = addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt by a blocking creature")
    void doesNotPreventCombatDamageFromBlocker() {
        castAndResolve();

        Permanent attacker = addAttacker();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage during combat")
    void doesNotPreventNoncombatDamage() {
        castAndResolve();
        Permanent attacker = addAttacker();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Prevention expires when the turn ends")
    void preventionExpiresAtEndOfTurn() {
        castAndResolve();
        harness.setLibrary(player2, List.of(new NestInvader()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player1, 20);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new HarmlessAssault(), "{2}{W}{W}");

        harness.passBothPriorities();
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
