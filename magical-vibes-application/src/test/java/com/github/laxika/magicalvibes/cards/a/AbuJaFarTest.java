package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AbuJaFarTest extends BaseCardTest {

    @Test
    @DisplayName("When Abu Ja'far dies while blocked, it destroys all creatures blocking it")
    void destroysCreaturesBlockingIt() {
        harness.addToBattlefield(player1, new AbuJaFar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent abu = findPermanent(player1, "Abu Ja'far");
        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        UUID blockerId = blocker.getId();
        setupCombat(abu, blocker);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Abu Ja'far");
        assertThat(harness.getGameData().stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Abu Ja'far")
                        && entry.getTargetIds().contains(blockerId));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("When Abu Ja'far dies while blocking, it destroys the creature it blocked")
    void destroysCreatureItBlocked() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AbuJaFar());

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        Permanent abu = findPermanent(player2, "Abu Ja'far");
        UUID attackerId = attacker.getId();
        setupCombat(attacker, abu);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Abu Ja'far");
        assertThat(harness.getGameData().stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Abu Ja'far")
                        && entry.getTargetIds().contains(attackerId));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void setupCombat(Permanent attacker, Permanent blocker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
