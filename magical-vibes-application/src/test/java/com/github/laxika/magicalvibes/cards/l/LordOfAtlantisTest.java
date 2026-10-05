package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordOfAtlantis.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class, Island.class,
        Forest.class})
class LordOfAtlantisTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk get +1/+1 and islandwalk")
    void buffsOtherMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        harness.addToBattlefield(player1, new LordOfAtlantis());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Lord of Atlantis does not buff itself")
    void doesNotBuffItself() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LordOfAtlantis());

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lord, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LordOfAtlantis());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Merfolk too")
    void buffsOpponentMerfolk() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player2, new MerfolkOfThePearlTrident());

        Permanent opponentMerfolk = merfolk(player2);

        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentMerfolk, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Two Lords stack their bonuses and buff each other")
    void twoLordsStack() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());

        Permanent merfolk = merfolk(player1);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(3);

        for (Permanent lord : findPermanents(player1, "Lord of Atlantis")) {
            assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, lord, Keyword.ISLANDWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Bonus is removed when Lord of Atlantis leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());

        Permanent merfolk = merfolk(player1);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lord of Atlantis"));

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Merfolk with islandwalk cannot be blocked when defender controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player2, new Island());

        Permanent attacker = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        attacker.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

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

        Permanent attacker = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        attacker.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    void islandwalkAllowsBlockingWhenDefenderControlsNonIslandLand() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player2, new Forest());

        Permanent attacker = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        attacker.setAttacking(true);
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    private Permanent merfolk(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Merfolk of the Pearl Trident");
    }

    @Test
    void opposingLordsBuffEachOtherAndBothPlayersMerfolk() {
        Permanent firstLord = harness.addToBattlefieldAndReturn(player1, new LordOfAtlantis());
        Permanent secondLord = harness.addToBattlefieldAndReturn(player2, new LordOfAtlantis());
        Permanent firstMerfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        Permanent secondMerfolk = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());

        for (Permanent creature : List.of(firstLord, secondLord, firstMerfolk, secondMerfolk)) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.ISLANDWALK)).isTrue();
        }
    }

    @Test
    void attackersIslandDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new LordOfAtlantis());
        harness.addToBattlefield(player1, new Island());
        Permanent attacker = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
