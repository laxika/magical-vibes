package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkSpy.class, RuneclawBear.class, Island.class})
class MerfolkSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to player reveals a card from opponent's hand")
    void combatDamageTriggersReveal() {
        harness.setHand(player2, List.of(new RuneclawBear()));

        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        // Player2's hand should remain unchanged (reveal, not discard)
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Runeclaw Bear");

        // Game log records the reveal
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals") && log.contains("at random"));
    }

    @Test
    @DisplayName("Revealed card stays in hand — it is not discarded")
    void revealedCardStaysInHand() {
        RuneclawBear bears1 = new RuneclawBear();
        RuneclawBear bears2 = new RuneclawBear();
        harness.setHand(player2, List.of(bears1, bears2));

        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No reveal when opponent has empty hand")
    void noRevealWhenEmptyHand() {
        harness.setHand(player2, List.of());

        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to reveal"));
    }

    @Test
    @DisplayName("No trigger when Merfolk Spy is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        harness.setHand(player2, List.of(new RuneclawBear()));

        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int logSizeBefore = gd.gameLog.size();

        resolveCombat();
        resolveAllTriggers();

        // No reveal log entries should appear
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText).skip(logSizeBefore))
                .noneMatch(log -> log.contains("reveals") && log.contains("at random"));
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Merfolk Spy")
    void dealsCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new RuneclawBear()));

        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new RuneclawBear());
        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(spy);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player does not control an Island")
    void canBeBlockedWithoutIsland() {
        Permanent blockerPerm = addCreatureReady(player2, new RuneclawBear());
        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(spy);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Player two's Spy reveals from the damaged player's hand")
    void playerTwoSpyRevealsFromPlayerOneHand() {
        RuneclawBear card = new RuneclawBear();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        Permanent spy = addCreatureReady(player2, new MerfolkSpy());
        spy.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains(player1.getUsername() + " reveals Runeclaw Bear at random."));
    }

    @Test
    @DisplayName("An Island controlled only by the attacker does not prevent blocking")
    void attackersIslandDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new Island());
        Permanent spy = addCreatureReady(player1, new MerfolkSpy());
        spy.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spy);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

}
