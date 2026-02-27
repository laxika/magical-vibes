package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DeathtouchMechanicTest extends BaseCardTest {

    /**
     * Helper: creates a card with deathtouch granted.
     */
    private Card withDeathtouch(Card card) {
        card.setKeywords(Set.of(Keyword.DEATHTOUCH));
        return card;
    }

    private Card withKeywords(Card card, Keyword... keywords) {
        card.setKeywords(Set.of(keywords));
        return card;
    }

    // ===== 1. Deathtouch 1/1 kills a 5/5 in combat =====

    @Test
    @DisplayName("Deathtouch 1/1 kills a 5/5 blocker — both die")
    void deathtouchOneOneKillsFiveFive() {
        // 1/1 with deathtouch attacks, 5/5 blocks
        Card dtCard = withDeathtouch(new LlanowarElves());
        Card bigCard = new AvatarOfMight(); // 8/8 trample
        bigCard.setKeywords(Set.of()); // remove trample for this test

        harness.addToBattlefield(player1, dtCard);
        harness.addToBattlefield(player2, bigCard);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both should be dead: 1/1 deathtouch kills 8/8, 8/8 kills 1/1
        harness.assertNotOnBattlefield(player1, dtCard.getName());
        harness.assertNotOnBattlefield(player2, bigCard.getName());
    }

    // ===== 2. Deathtouch creature blocked by multiple blockers (auto-assigns 1 each) =====

    @Test
    @DisplayName("Deathtouch attacker blocked by two creatures auto-assigns 1 damage each")
    void deathtouchMultipleBlockersAutoAssign() {
        // 2/2 deathtouch attacks, blocked by two 2/2s
        Card dtCard = withDeathtouch(new GrizzlyBears());
        harness.addToBattlefield(player1, dtCard);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<Permanent> atkBf = gd.playerBattlefields.get(player1.getId());
        Permanent attacker = atkBf.getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance to combat damage — needs manual assignment (2 blockers)
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

        // Assign 1 damage to each blocker (deathtouch makes 1 lethal)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 1,
                blocker2.getId(), 1
        ));

        // Both blockers should be dead (1 damage from deathtouch is lethal)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== 3. Deathtouch + trample: excess goes to player =====

    @Test
    @DisplayName("Deathtouch + trample: 1 to blocker, rest tramples through")
    void deathtouchTrampleExcessToPlayer() {
        // 5/5 deathtouch+trample attacks, blocked by a 4/4
        Card dtTrampleCard = withKeywords(new AvatarOfMight(), Keyword.DEATHTOUCH, Keyword.TRAMPLE);
        // AvatarOfMight is 8/8; let's use GrizzlyBears as blocker (2/2)
        harness.addToBattlefield(player1, dtTrampleCard);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker = defBf.getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance to combat damage — needs manual assignment (trample)
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

        // With deathtouch+trample, only 1 to blocker is needed, 7 tramples through
        // AvatarOfMight is 8/8
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 7
        ));

        // Blocker dies (1 deathtouch damage is lethal)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took 7 damage
        harness.assertLife(player2, 13);
    }

    // ===== 4. Deathtouch + indestructible: blocker survives =====

    @Test
    @DisplayName("Deathtouch cannot destroy indestructible creature")
    void deathtouchVsIndestructible() {
        Card dtCard = withDeathtouch(new LlanowarElves()); // 1/1 deathtouch
        Card indestructibleCard = new GrizzlyBears();
        indestructibleCard.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));

        harness.addToBattlefield(player1, dtCard);
        harness.addToBattlefield(player2, indestructibleCard);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Deathtouch attacker dies (1/1 vs 2/2)
        harness.assertNotOnBattlefield(player1, dtCard.getName());
        // Indestructible blocker survives despite deathtouch
        harness.assertOnBattlefield(player2, indestructibleCard.getName());
    }

    // ===== 5. Deathtouch blocker kills attacker =====

    @Test
    @DisplayName("Deathtouch 1/1 blocker kills a 5/5 attacker")
    void deathtouchBlockerKillsAttacker() {
        Card bigCard = new AvatarOfMight();
        bigCard.setKeywords(Set.of()); // remove trample
        Card dtBlocker = withDeathtouch(new LlanowarElves()); // 1/1 deathtouch

        harness.addToBattlefield(player1, bigCard);
        harness.addToBattlefield(player2, dtBlocker);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // 8/8 attacker dies (1 deathtouch damage is lethal)
        harness.assertNotOnBattlefield(player1, bigCard.getName());
        // 1/1 deathtouch blocker dies (8 damage from attacker)
        harness.assertNotOnBattlefield(player2, dtBlocker.getName());
    }

    // ===== 6. Deathtouch damage fully prevented — creature survives =====

    @Test
    @DisplayName("Deathtouch creature with 0 damage after prevention shield does not kill")
    void deathtouchDamagePreventedCreatureSurvives() {
        Card dtCard = withDeathtouch(new LlanowarElves()); // 1/1 deathtouch
        harness.addToBattlefield(player1, dtCard);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Give blocker a prevention shield of 1 (preventing all deathtouch damage)
        blocker.setDamagePreventionShield(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // 1/1 deathtouch attacker dies (took 2 damage from blocker)
        harness.assertNotOnBattlefield(player1, dtCard.getName());
        // 2/2 blocker survives (deathtouch damage was fully prevented)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
