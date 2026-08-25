package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalhaulerSwine.class, FugitiveWizard.class, Shock.class})
class CoalhaulerSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes Coalhauler Swine deal that much damage to each player")
    void nonCombatDamageHitsEachPlayer() {
        addReadySwine(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID swineId = harness.getPermanentId(player2, "Coalhauler Swine");
        harness.castInstant(player1, 0, swineId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Coalhauler Swine");
    }

    @Test
    @DisplayName("Combat damage makes Coalhauler Swine deal that much damage to each player")
    void combatDamageHitsEachPlayer() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent swine = addReadySwine(player2);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        swine.setBlocking(true);
        swine.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Coalhauler Swine");
    }

    private Permanent addReadySwine(com.github.laxika.magicalvibes.model.Player owner) {
        harness.addToBattlefield(owner, new CoalhaulerSwine());
        Permanent swine = gd.playerBattlefields.get(owner.getId()).getFirst();
        swine.setSummoningSick(false);
        return swine;
    }
}
