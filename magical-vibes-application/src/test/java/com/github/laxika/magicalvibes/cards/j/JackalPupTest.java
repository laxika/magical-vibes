package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JackalPupTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage: Jackal Pup deals that much damage to its controller")
    void nonCombatDamageReflectedToController() {
        harness.addToBattlefield(player2, new JackalPup()); // 2/1
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        UUID pupId = harness.getPermanentId(player2, "Jackal Pup");
        harness.castInstant(player1, 0, pupId);
        harness.passBothPriorities(); // Shock deals 2, ON_DEALT_DAMAGE queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Jackal Pup"); // 2/1 dies to 2 damage
    }

    @Test
    @DisplayName("Combat damage: Jackal Pup's controller takes the combat damage dealt to it")
    void combatDamageReflectedToController() {
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1
        harness.addToBattlefield(player2, new JackalPup()); // 2/1
        harness.setLife(player2, 20);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent pup = gd.playerBattlefields.get(player2.getId()).getFirst();
        pup.setSummoningSick(false);
        pup.setBlocking(true);
        pup.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // Combat damage: Pup takes 1, trigger queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Jackal Pup"); // 2/1 dies to 1 damage
    }
}
