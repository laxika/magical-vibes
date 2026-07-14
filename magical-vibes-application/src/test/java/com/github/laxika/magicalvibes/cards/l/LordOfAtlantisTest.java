package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LordOfAtlantisTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk get +1/+1 and islandwalk")
    void buffsOtherMerfolk() {
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new LordOfAtlantis());

        Permanent merfolk = merfolk(player1);

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Lord of Atlantis does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new LordOfAtlantis());

        Permanent lord = lord(player1);

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lord, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LordOfAtlantis());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Merfolk too")
    void buffsOpponentMerfolk() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player2, new CoralMerfolk());

        Permanent opponentMerfolk = merfolk(player2);

        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentMerfolk, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Two Lords stack their bonuses and buff each other")
    void twoLordsStack() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new CoralMerfolk());

        Permanent merfolk = merfolk(player1);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(3);

        for (Permanent lord : gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lord of Atlantis")).toList()) {
            assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, lord, Keyword.ISLANDWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Bonus is removed when Lord of Atlantis leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new CoralMerfolk());

        Permanent merfolk = merfolk(player1);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lord of Atlantis"));

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Merfolk with islandwalk cannot be blocked when defender controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player2, new Island());

        Permanent attacker = new Permanent(new CoralMerfolk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Merfolk with islandwalk can be blocked when defender controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        harness.addToBattlefield(player1, new LordOfAtlantis());

        Permanent attacker = new Permanent(new CoralMerfolk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    private Permanent merfolk(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Coral Merfolk"))
                .findFirst().orElseThrow();
    }

    private Permanent lord(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lord of Atlantis"))
                .findFirst().orElseThrow();
    }
}
