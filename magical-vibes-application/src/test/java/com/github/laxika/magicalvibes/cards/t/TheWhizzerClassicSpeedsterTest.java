package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({TheWhizzerClassicSpeedster.class, HonorGuard.class})
class TheWhizzerClassicSpeedsterTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 1/1 before it deals regular damage")
    void firstStrikeKillsBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new TheWhizzerClassicSpeedster());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new HonorGuard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "The Whizzer, Classic Speedster");
        harness.assertInGraveyard(player2, "Honor Guard");
    }

    @Test
    @DisplayName("Haste allows attacking immediately after resolving")
    void hasteAllowsAttackingImmediately() {
        harness.setHand(player1, List.of(new TheWhizzerClassicSpeedster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
