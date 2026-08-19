package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TroubledHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a land and prevents the next 2 damage to a player")
    void sacrificesLandAndPreventsNextTwoDamageToPlayer() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.addToBattlefield(player2, new MoggFanatic());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Forest");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player2, 0, null, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents damage to a target creature")
    void preventsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new TroubledHealer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new MoggFanatic());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }
}
