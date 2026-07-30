package com.github.laxika.magicalvibes.cards.m;

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

class MasterOfThePearlTridentTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk you control get +1/+1 and islandwalk")
    void buffsOtherOwnMerfolk() {
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());

        Permanent merfolk = findPermanent(player1, "Coral Merfolk");

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());

        Permanent master = findPermanent(player1, "Master of the Pearl Trident");

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, master, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Merfolk")
    void doesNotBuffOpponentMerfolk() {
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());
        harness.addToBattlefield(player2, new CoralMerfolk());

        Permanent opponentMerfolk = findPermanent(player2, "Coral Merfolk");

        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentMerfolk, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Two Masters stack their bonuses and buff each other")
    void twoMastersStack() {
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());
        harness.addToBattlefield(player1, new CoralMerfolk());

        Permanent merfolk = findPermanent(player1, "Coral Merfolk");
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(3);

        for (Permanent master : findPermanents(player1, "Master of the Pearl Trident")) {
            assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, master, Keyword.ISLANDWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Bonus is removed when the Master leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());
        harness.addToBattlefield(player1, new CoralMerfolk());

        Permanent merfolk = findPermanent(player1, "Coral Merfolk");
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Master of the Pearl Trident"));

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Granted islandwalk stops a block when the defender controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        harness.addToBattlefield(player1, new MasterOfThePearlTrident());
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
}
