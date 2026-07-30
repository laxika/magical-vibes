package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GloomSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Lethal combat damage is prevented and that many cards are exiled from the top of its controller's library")
    void combatDamagePreventedAndCardsExiled() {
        // Gloom Surgeon attacks and is blocked by a 3/3 — the blocker assigns all 3 damage to it.
        Permanent surgeon = harness.addToBattlefieldAndReturn(player1, new GloomSurgeon());
        surgeon.setSummoningSick(false);
        surgeon.setAttacking(true);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        Permanent blocker = new Permanent(new HillGiant());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Hill Giant's 3 (lethal) combat damage was prevented — the 2/1 survives undamaged.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(surgeon.getId()));
        assertThat(surgeon.getMarkedDamage()).isZero();
        // 3 cards were exiled from the top of Gloom Surgeon's controller's library.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.exiledCards).hasSize(3)
                .allMatch(e -> e.ownerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Noncombat damage is not prevented and exiles nothing")
    void noncombatDamageStillLands() {
        Permanent surgeon = harness.addToBattlefieldAndReturn(player2, new GloomSurgeon());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, surgeon.getId());
        harness.passBothPriorities();

        // Shock kills the 2/1; no library exile happened.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(surgeon.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Combat damage lands when damage can't be prevented this turn")
    void unpreventableCombatDamageLands() {
        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent surgeon = harness.addToBattlefieldAndReturn(player2, new GloomSurgeon());
        surgeon.setSummoningSick(false);
        surgeon.setBlocking(true);
        surgeon.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        gd.damageCantBePreventedThisTurn = true;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(surgeon.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }
}
