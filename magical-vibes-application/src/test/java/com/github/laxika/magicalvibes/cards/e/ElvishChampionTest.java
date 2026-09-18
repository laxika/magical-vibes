package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({ElvishChampion.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class ElvishChampionTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Elvish Champion puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new ElvishChampion(), "{1}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(ElvishChampion.class);
    }

    @Test
    @DisplayName("Resolving puts Elvish Champion onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new ElvishChampion(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Elvish Champion");
    }

    @Test
    @DisplayName("Other Elf creatures get +1/+1 and forestwalk")
    void buffsOtherElves() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new ElvishChampion());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Elvish Champion does not buff itself")
    void doesNotBuffItself() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ElvishChampion());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, champion, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Elf creatures")
    void doesNotBuffNonElves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ElvishChampion());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Elf creatures too")
    void buffsOpponentElves() {
        harness.addToBattlefield(player1, new ElvishChampion());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Two Elvish Champions buff each other")
    void twoChampionsBuffEachOther() {
        Permanent firstChampion = harness.addToBattlefieldAndReturn(player1, new ElvishChampion());
        Permanent secondChampion = harness.addToBattlefieldAndReturn(player1, new ElvishChampion());

        for (Permanent champion : List.of(firstChampion, secondChampion)) {
            assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, champion, Keyword.FORESTWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Elvish Champions give +2/+2 to other Elves")
    void twoChampionsStackBonuses() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new ElvishChampion());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Elvish Champion leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ElvishChampion());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(champion);

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Elvish Champion resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);

        harness.castFromHand(player1, new ElvishChampion(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new ElvishChampion());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        elf.setPowerModifier(elf.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(7);

        elf.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Elf with forestwalk cannot be blocked when defender controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player2, new Forest());

        Permanent elfAttacker = addCreatureReady(player1, new LlanowarElves());
        elfAttacker.setAttacking(true);
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(elfAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Elf with forestwalk can be blocked when defender does not control a Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        harness.addToBattlefield(player1, new ElvishChampion());

        Permanent elfAttacker = addCreatureReady(player1, new LlanowarElves());
        elfAttacker.setAttacking(true);
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(elfAttacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Elf with forestwalk can be blocked when only the attacking player controls a Forest")
    void forestwalkIgnoresAttackingPlayersForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ElvishChampion());

        Permanent elfAttacker = addCreatureReady(player1, new LlanowarElves());
        elfAttacker.setAttacking(true);
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(elfAttacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Elvish Champion itself does not have forestwalk when alone")
    void championDoesNotHaveForestwalkItself() {
        harness.addToBattlefield(player2, new Forest());

        Permanent champion = addCreatureReady(player1, new ElvishChampion());
        champion.setAttacking(true);
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(champion);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk is lost when Elvish Champion leaves the battlefield")
    void forestwalkLostWhenChampionLeaves() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ElvishChampion());
        harness.addToBattlefield(player2, new Forest());

        Permanent elfAttacker = addCreatureReady(player1, new LlanowarElves());

        assertThat(gqs.hasKeyword(gd, elfAttacker, Keyword.FORESTWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(champion);

        assertThat(gqs.hasKeyword(gd, elfAttacker, Keyword.FORESTWALK)).isFalse();
    }
}
