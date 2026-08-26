package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumberingLaundry.class, MasterOfPearls.class})
class LumberingLaundryTest extends BaseCardTest {

    @Test
    void controllerCanSeeOpposingFaceDownCreatureUntilEndOfTurn() {
        addCreatureReady(player1, new LumberingLaundry());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isEmpty();
    }

    @Test
    void permissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new LumberingLaundry());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearMessages();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isEmpty();
    }
}
