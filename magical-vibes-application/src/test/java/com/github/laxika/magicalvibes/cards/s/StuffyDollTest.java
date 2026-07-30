package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StuffyDollTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: deals 1 damage to itself, which reflects 1 damage to the chosen player")
    void tapAbilityReflectsOneDamage() {
        addReadyDoll(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // 1 damage to itself, ON_DEALT_DAMAGE queued
        harness.passBothPriorities(); // reflected damage resolves

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // 0/1 with 1 damage marked, but indestructible keeps it on the battlefield
        harness.assertOnBattlefield(player1, "Stuffy Doll");
    }

    @Test
    @DisplayName("Damage from an opponent's spell reflects that much damage to the chosen player")
    void spellDamageReflected() {
        addReadyDoll(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID dollId = harness.getPermanentId(player1, "Stuffy Doll");
        harness.castInstant(player2, 0, dollId);
        harness.passBothPriorities(); // Shock deals 2, ON_DEALT_DAMAGE queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Stuffy Doll");
    }

    @Test
    @DisplayName("Combat damage taken reflects that much damage to the chosen player")
    void combatDamageReflected() {
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1
        Permanent doll = addReadyDoll(player2);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        doll.setBlocking(true);
        doll.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage: doll takes 1, trigger queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Stuffy Doll");
    }

    private Permanent addReadyDoll(Player owner) {
        harness.addToBattlefield(owner, new StuffyDoll());
        Permanent doll = gd.playerBattlefields.get(owner.getId()).getFirst();
        doll.setSummoningSick(false);
        return doll;
    }
}
