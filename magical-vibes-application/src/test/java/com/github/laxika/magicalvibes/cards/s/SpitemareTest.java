package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpitemareTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage: Spitemare deals that much damage to a chosen player")
    void nonCombatDamageReflectedToPlayer() {
        harness.addToBattlefield(player2, new Spitemare()); // 3/3
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID spitemareId = harness.getPermanentId(player2, "Spitemare");
        harness.castInstant(player1, 0, spitemareId);
        harness.passBothPriorities(); // Shock deals 2 to Spitemare, ON_DEALT_DAMAGE queued

        // Spitemare's controller chooses to redirect the 2 damage at player1
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Spitemare"); // 3/3 survives 2 damage
    }

    @Test
    @DisplayName("Non-combat damage: Spitemare deals that much damage to a chosen creature")
    void nonCombatDamageReflectedToCreature() {
        harness.addToBattlefield(player2, new Spitemare()); // 3/3
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID spitemareId = harness.getPermanentId(player2, "Spitemare");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, spitemareId);
        harness.passBothPriorities(); // Shock deals 2 to Spitemare, ON_DEALT_DAMAGE queued

        harness.handlePermanentChosen(player2, bearsId);
        harness.passBothPriorities();

        // 2 damage is lethal for the 2/2 Grizzly Bears
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage: Spitemare deals combat damage taken to a chosen target")
    void combatDamageReflected() {
        harness.addToBattlefield(player2, new Spitemare()); // 3/3
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.setLife(player1, 20);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent spitemare = gd.playerBattlefields.get(player2.getId()).getFirst();
        spitemare.setSummoningSick(false);
        spitemare.setBlocking(true);
        spitemare.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // Combat damage: Spitemare takes 2, trigger queued

        // Reflect the 2 combat damage taken at player1
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears"); // 2/2 killed by Spitemare's 3 power
    }
}
