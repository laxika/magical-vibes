package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloomSurgeon.class, HillGiant.class, GrizzlyBears.class, Shock.class, Malignus.class, TurnToFrog.class})
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

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

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

        harness.castAndResolveInstant(player1, 0, surgeon.getId());

        // Shock kills the 2/1; no library exile happened.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(surgeon.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Unpreventable combat damage lands and still exiles cards")
    void unpreventableCombatDamageLands() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

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
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3)
                .allMatch(e -> e.ownerId().equals(player2.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Combat damage is fully prevented even with too few cards to exile")
    void shortLibraryDoesNotLimitPrevention(int librarySize) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GloomSurgeon());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent surgeon = harness.addToBattlefieldAndReturn(player2, new GloomSurgeon());
        surgeon.setBlocking(true);
        surgeon.addBlockingTarget(0);
        List<GloomSurgeon> library = new ArrayList<>();
        for (int i = 0; i < librarySize; i++) {
            library.add(new GloomSurgeon());
        }
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, library);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(surgeon);
        assertThat(surgeon.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(librarySize)
                .allMatch(e -> e.ownerId().equals(player2.getId()));
        assertThat(gd.exiledCards.stream().map(e -> e.card()).toList())
                .containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Malignus's unpreventable combat damage still causes library exile")
    void unpreventableSourceStillExilesCards() {
        gd.playerLifeTotals.put(player2.getId(), 6);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Malignus());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent surgeon = harness.addToBattlefieldAndReturn(player2, new GloomSurgeon());
        surgeon.setBlocking(true);
        surgeon.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new GloomSurgeon(), new GloomSurgeon(), new GloomSurgeon()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(surgeon);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3)
                .allMatch(e -> e.ownerId().equals(player2.getId()));
    }

    @Test
    @DisplayName("Losing all abilities removes combat damage prevention and library exile")
    void lostAbilityDoesNotPreventOrExile() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GloomSurgeon());
        attacker.setSummoningSick(false);
        Permanent surgeon = harness.addToBattlefieldAndReturn(player2, new GloomSurgeon());
        harness.setLibrary(player2, List.of(new GloomSurgeon(), new GloomSurgeon(), new GloomSurgeon()));
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, surgeon.getId());

        attacker.setAttacking(true);
        surgeon.setBlocking(true);
        surgeon.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(surgeon);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards.stream().filter(e -> e.ownerId().equals(player2.getId())))
                .isEmpty();
    }
}
