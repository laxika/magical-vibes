package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwansOfBrynArgollTest extends BaseCardTest {

    @Test
    @DisplayName("Burn spell to Swans is prevented and the source's controller draws that many cards")
    void burnDamagePreventedSourceControllerDraws() {
        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, swans.getId());
        harness.passBothPriorities();

        // Swans took no damage and survives.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(swans.getId()));
        assertThat(swans.getMarkedDamage()).isZero();
        // Shock's controller (player1) drew 2 cards; Swans' controller (player2) drew nothing.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Lethal combat damage to Swans is prevented and the attacker's controller draws")
    void combatDamagePreventedSourceControllerDraws() {
        // A 3/3 attacker blocked by the 4/3 Swans would deal lethal (3) damage to it.
        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        swans.setSummoningSick(false);
        swans.setBlocking(true);
        swans.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The 3 (lethal) combat damage to Swans was prevented — it survives.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(swans.getId()));
        assertThat(swans.getMarkedDamage()).isZero();
        // The attacker's controller (player1) drew 3 cards.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("When damage can't be prevented, Swans takes damage and the source's controller draws nothing")
    void unpreventableDamageStillLandsNoDraw() {
        Permanent swans = harness.addToBattlefieldAndReturn(player2, new SwansOfBrynArgoll());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        gd.damageCantBePreventedThisTurn = true;

        harness.castInstant(player1, 0, swans.getId());
        harness.passBothPriorities();

        assertThat(swans.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }
}
