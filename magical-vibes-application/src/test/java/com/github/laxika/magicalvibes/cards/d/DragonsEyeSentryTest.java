package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsEyeSentry.class, GrizzlyBears.class})
class DragonsEyeSentryTest extends BaseCardTest {

    @Test
    void cannotAttackBecauseItHasDefender() {
        Permanent sentry = new Permanent(new DragonsEyeSentry());
        sentry.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sentry);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstStrikeDealsCombatDamageBeforeBlocker() {
        DragonsEyeSentry sentry = new DragonsEyeSentry();
        Permanent attacker = new Permanent(sentry);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(1);
        bears.setToughness(1);
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon's Eye Sentry");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
