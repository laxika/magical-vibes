package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class FightToTheDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys blocked attackers and blocking creatures, spares uninvolved creatures")
    void destroysBlockedAndBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        // Not in combat — should survive.
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spares an unblocked attacker and creatures not in combat")
    void sparesUnblockedAndNonCombatants() {
        Permanent unblockedAttacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        unblockedAttacker.setSummoningSick(false);
        unblockedAttacker.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FightToTheDeath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
