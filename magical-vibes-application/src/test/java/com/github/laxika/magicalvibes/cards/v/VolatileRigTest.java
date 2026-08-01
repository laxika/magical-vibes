package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolatileRigTest extends BaseCardTest {

    @Test
    @DisplayName("Volatile Rig must attack each combat if able")
    void mustAttackWhenAble() {
        addReadyRig(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("When Volatile Rig is dealt damage, a lost flip sacrifices it")
    void dealtDamageFlipsForSacrifice() {
        Permanent rig = addReadyRig(player1);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        rig.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));
        harness.passBothPriorities();

        boolean onBattlefield = gd.playerBattlefields.get(player1.getId()).contains(rig);
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(card -> card.getName().equals("Volatile Rig"));

        assertThat(onBattlefield != inGraveyard).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Volatile Rig"));
    }

    @Test
    @DisplayName("When Volatile Rig dies, a lost flip deals 4 damage to creatures and players")
    void deathFlipsForMassDamage() {
        Permanent rig = harness.addToBattlefieldAndReturn(player1, new VolatileRig());
        Permanent bear = addReadyCreature(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, rig.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());
        boolean massDamageResolved = player1Life == 16 && player2Life == 16;
        boolean flipWon = player1Life == 20 && player2Life == 20;

        assertThat(massDamageResolved || flipWon).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Volatile Rig"));

        boolean bearOnBattlefield = gd.playerBattlefields.get(player2.getId()).contains(bear);
        assertThat(bearOnBattlefield).isEqualTo(flipWon);
    }

    private Permanent addReadyRig(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new VolatileRig());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
