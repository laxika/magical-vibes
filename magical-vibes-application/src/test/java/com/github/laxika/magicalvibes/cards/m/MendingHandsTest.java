package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
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

class MendingHandsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mending Hands adds a 4-damage prevention shield to target creature")
    void resolvingAddsCreaturePrevention() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolving Mending Hands targeting a player adds a 4-damage prevention shield")
    void resolvingAddsPlayerPrevention() {
        harness.setHand(player1, List.of(new MendingHands()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Player prevention shield reduces unblocked combat damage")
    void playerPreventionReducesCombatDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().playerDamagePreventionShields.put(player2.getId(), 4);

        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 2 combat damage fully prevented (shield 4 >= 2) → life unchanged, 2 of shield consumed
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevention shields are cleared at end of turn")
    void preventionShieldsClearedAtEndOfTurn() {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setDamagePreventionShield(4);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        harness.getGameData().playerDamagePreventionShields.put(player1.getId(), 4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(afterCleanup.getDamagePreventionShield()).isEqualTo(0);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }
}
