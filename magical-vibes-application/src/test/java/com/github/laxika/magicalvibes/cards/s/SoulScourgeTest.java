package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SoulScourge.class, Murder.class})
class SoulScourgeTest extends BaseCardTest {

    @Test
    void targetedPlayerLosesLifeOnEnterAndRegainsItOnLeave() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castSoulScourgeWithTarget(player2.getId());

        harness.assertLife(player2, 17);

        destroySoulScourge();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void leavesTriggerUsesEtbTargetWhenEtbIsStillOnStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SoulScourge()));
        addSoulScourgeMana();
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        destroySoulScourge();
        harness.passBothPriorities();
        harness.assertLife(player2, 23);

        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    private void castSoulScourgeWithTarget(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SoulScourge()));
        addSoulScourgeMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addSoulScourgeMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void destroySoulScourge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Soul Scourge"));
        harness.passBothPriorities();
    }
}
