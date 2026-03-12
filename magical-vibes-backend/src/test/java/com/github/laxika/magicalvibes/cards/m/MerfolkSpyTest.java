package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerfolkSpyTest extends BaseCardTest {

    private Permanent addReadyCreature(Card card, boolean forPlayer1) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        var playerId = forPlayer1 ? player1.getId() : player2.getId();
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER with RevealRandomCardFromTargetPlayerHandEffect")
    void hasCorrectEffect() {
        MerfolkSpy card = new MerfolkSpy();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(RevealRandomCardFromTargetPlayerHandEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage to player reveals a card from opponent's hand")
    void combatDamageTriggersReveal() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        // Player2's hand should remain unchanged (reveal, not discard)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");

        // Game log records the reveal
        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals") && log.contains("at random"));
    }

    @Test
    @DisplayName("Revealed card stays in hand — it is not discarded")
    void revealedCardStaysInHand() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.setHand(player2, List.of(bears1, bears2));

        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No reveal when opponent has empty hand")
    void noRevealWhenEmptyHand() {
        harness.setHand(player2, List.of());

        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);

        resolveCombat();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to reveal"));
    }

    @Test
    @DisplayName("No trigger when Merfolk Spy is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);

        Permanent blocker = addReadyCreature(new GrizzlyBears(), false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int logSizeBefore = gd.gameLog.size();

        resolveCombat();

        // No reveal log entries should appear
        assertThat(gd.gameLog.stream().skip(logSizeBefore))
                .noneMatch(log -> log.contains("reveals") && log.contains("at random"));
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Merfolk Spy")
    void dealsCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Islandwalk =====

    @Test
    @DisplayName("Cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blockerPerm = addReadyCreature(new GrizzlyBears(), false);
        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(spy);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player does not control an Island")
    void canBeBlockedWithoutIsland() {
        Permanent blockerPerm = addReadyCreature(new GrizzlyBears(), false);
        Permanent spy = addReadyCreature(new MerfolkSpy(), true);
        spy.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(spy);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
