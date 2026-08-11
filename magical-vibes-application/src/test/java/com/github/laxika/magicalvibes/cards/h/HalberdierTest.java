package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HalberdierTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Halberdier destroy a 2/2 blocker before it deals combat damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        Permanent attacker = new Permanent(new Halberdier());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(2);
        blockerCard.setToughness(2);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Halberdier");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
