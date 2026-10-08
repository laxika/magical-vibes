package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({SoulScourge.class, FieryTemper.class})
class SoulScourgeTest extends BaseCardTest {

    @Test
    void targetedPlayerLosesLifeOnEnterAndRegainsItOnLeave() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castSoulScourgeWithTarget(player2.getId());

        harness.assertLife(player2, 17);

        removeSoulScourge();
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

        removeSoulScourge();
        harness.passBothPriorities();
        harness.assertLife(player2, 23);

        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerCanBeTargetedAndRegainsLifeOnLeave() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castSoulScourgeWithTarget(player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);

        removeSoulScourge();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachSoulScourgeRemembersItsOwnPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castSoulScourgeWithTarget(player2.getId());
        UUID firstScourgeId = harness.getPermanentId(player1, "Soul Scourge");
        castSoulScourgeWithTarget(player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);

        removeSoulScourge(firstScourgeId);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Soul Scourge");

        removeSoulScourge();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Soul Scourge");
    }

    private void castSoulScourgeWithTarget(UUID targetId) {
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

    private void removeSoulScourge() {
        removeSoulScourge(harness.getPermanentId(player1, "Soul Scourge"));
    }

    private void removeSoulScourge(UUID permanentId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }
}
