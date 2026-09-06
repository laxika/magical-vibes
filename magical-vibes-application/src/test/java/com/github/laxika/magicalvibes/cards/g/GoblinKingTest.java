package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinKing.class, GoblinBalloonBrigade.class, GrizzlyBears.class, Mountain.class})
class GoblinKingTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Goblin King puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new GoblinKing(), "{1}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Goblin King onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new GoblinKing(), "{1}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    // ===== Static effect: buffs other Goblins =====

    @Test
    @DisplayName("Other Goblin creatures get +1/+1 and mountainwalk")
    void buffsOtherGoblins() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBalloonBrigade());
        harness.addToBattlefield(player1, new GoblinKing());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Goblin King does not buff itself")
    void doesNotBuffItself() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new GoblinKing());

        assertThat(gqs.getEffectivePower(gd, king)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, king)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, king, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Goblin creatures")
    void doesNotBuffNonGoblins() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinKing());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Goblin creatures too")
    void buffsOpponentGoblins() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinBalloonBrigade());

        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentGoblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Goblin Kings buff each other")
    void twoKingsBuffEachOther() {
        Permanent firstKing = harness.addToBattlefieldAndReturn(player1, new GoblinKing());
        Permanent secondKing = harness.addToBattlefieldAndReturn(player1, new GoblinKing());

        for (Permanent king : List.of(firstKing, secondKing)) {
            assertThat(gqs.getEffectivePower(gd, king)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, king)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, king, Keyword.MOUNTAINWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Goblin Kings give +2/+2 to other Goblins")
    void twoKingsStackBonuses() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBalloonBrigade());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Goblin King leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new GoblinKing());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBalloonBrigade());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(king);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Goblin King resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBalloonBrigade());
        harness.castFromHand(player1, new GoblinKing(), "{1}{R}{R}");

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBalloonBrigade());

        goblin.setPowerModifier(goblin.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(7);

        goblin.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.MOUNTAINWALK)).isTrue();
    }

    // ===== Mountainwalk blocking =====

    @Test
    @DisplayName("Goblin with mountainwalk cannot be blocked when defender controls a Mountain")
    void mountainwalkPreventsBlockingWhenDefenderControlsMountain() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new Mountain());

        Permanent goblinAttacker = addCreatureReady(player1, new GoblinBalloonBrigade());
        goblinAttacker.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(goblinAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Goblin with mountainwalk can be blocked when defender does not control a Mountain")
    void mountainwalkAllowsBlockingWithoutMountain() {
        harness.addToBattlefield(player1, new GoblinKing());

        Permanent goblinAttacker = addCreatureReady(player1, new GoblinBalloonBrigade());
        goblinAttacker.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(goblinAttacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goblin King itself does not have mountainwalk (it only grants it)")
    void goblinKingDoesNotHaveMountainwalkItself() {
        Permanent king = addCreatureReady(player1, new GoblinKing());
        harness.addToBattlefield(player2, new Mountain());

        king.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(king);

        // Goblin King alone doesn't buff itself, so it has no mountainwalk and can be blocked
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk is lost when Goblin King leaves the battlefield")
    void mountainwalkLostWhenKingLeaves() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new GoblinKing());
        harness.addToBattlefield(player2, new Mountain());

        Permanent goblinAttacker = addCreatureReady(player1, new GoblinBalloonBrigade());

        // Verify mountainwalk is present
        assertThat(gqs.hasKeyword(gd, goblinAttacker, Keyword.MOUNTAINWALK)).isTrue();

        // Remove Goblin King
        gd.playerBattlefields.get(player1.getId()).remove(king);

        // Mountainwalk should be gone, allowing blocking
        assertThat(gqs.hasKeyword(gd, goblinAttacker, Keyword.MOUNTAINWALK)).isFalse();
    }
}

