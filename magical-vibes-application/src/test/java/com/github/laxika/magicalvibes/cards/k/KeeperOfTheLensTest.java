package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeeperOfTheLens.class, MasterOfPearls.class})
class KeeperOfTheLensTest extends BaseCardTest {

    @Test
    void controllerCanLookAtOpposingFaceDownCreatures() {
        harness.addToBattlefield(player1, new KeeperOfTheLens());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isEmpty();
    }

    @Test
    void permissionRemainsAfterEndOfTurn() {
        harness.addToBattlefield(player1, new KeeperOfTheLens());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isNotEmpty();
    }

    @Test
    void permissionEndsWhenKeeperLeavesTheBattlefield() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new KeeperOfTheLens());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, keeper));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isEmpty();
    }
}
