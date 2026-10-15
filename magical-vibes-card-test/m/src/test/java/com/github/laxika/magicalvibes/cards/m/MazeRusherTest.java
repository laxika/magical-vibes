package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BeetleformMage;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazeRusher.class, BeetleformMage.class, KraulWarrior.class})
class MazeRusherTest extends BaseCardTest {

    @Test
    @DisplayName("A multicolored creature you control gains haste")
    void grantsHasteToOwnMulticoloredCreature() {
        harness.addToBattlefield(player1, new MazeRusher());
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BeetleformMage());
        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A monocolored creature you control does not gain haste")
    void doesNotGrantHasteToMonocoloredCreature() {
        harness.addToBattlefield(player1, new MazeRusher());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's multicolored creature does not gain haste")
    void doesNotGrantHasteToOpponentCreature() {
        harness.addToBattlefield(player1, new MazeRusher());
        Permanent mage = harness.addToBattlefieldAndReturn(player2, new BeetleformMage());
        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The grant ends when Maze Rusher leaves the battlefield")
    void grantEndsWhenSourceLeaves() {
        Permanent rusher = harness.addToBattlefieldAndReturn(player1, new MazeRusher());
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BeetleformMage());
        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rusher);

        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Maze Rusher and a newly entered multicolored creature can attack immediately")
    void hasteAllowsAttackingWithSummoningSickness() {
        Permanent rusher = harness.addToBattlefieldAndReturn(player1, new MazeRusher());
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BeetleformMage());

        declareAttackers(List.of(0, 1));

        assertThat(rusher.isAttacking()).isTrue();
        assertThat(mage.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A multicolored creature already on the battlefield gains haste when Maze Rusher enters")
    void grantsHasteToExistingCreature() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BeetleformMage());
        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isFalse();

        harness.addToBattlefield(player1, new MazeRusher());

        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste persists while one of two Maze Rushers remains")
    void overlappingGrantsEndOnlyWhenBothSourcesLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MazeRusher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MazeRusher());
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new BeetleformMage());

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.HASTE)).isFalse();
    }
}
