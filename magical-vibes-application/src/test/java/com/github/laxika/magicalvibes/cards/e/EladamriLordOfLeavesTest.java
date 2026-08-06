package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EladamriLordOfLeavesTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elf creatures you control have forestwalk and shroud")
    void grantsToOwnElves() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EladamriLordOfLeaves()));
        Permanent elves = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(elves);

        assertThat(gqs.hasKeyword(gd, elves, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Elves an opponent controls are affected too")
    void grantsToOpponentElves() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EladamriLordOfLeaves()));
        Permanent enemyElves = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(enemyElves);

        assertThat(gqs.hasKeyword(gd, enemyElves, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyElves, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Eladamri grants nothing to itself")
    void doesNotGrantToItself() {
        Permanent eladamri = new Permanent(new EladamriLordOfLeaves());
        gd.playerBattlefields.get(player1.getId()).add(eladamri);

        assertThat(gqs.hasKeyword(gd, eladamri, Keyword.FORESTWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, eladamri, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Non-Elf creatures are unaffected")
    void doesNotGrantToNonElves() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new EladamriLordOfLeaves()));
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The grants end once Eladamri leaves the battlefield")
    void grantsEndWhenEladamriLeaves() {
        Permanent eladamri = new Permanent(new EladamriLordOfLeaves());
        gd.playerBattlefields.get(player1.getId()).add(eladamri);
        Permanent elves = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(elves);

        gd.playerBattlefields.get(player1.getId()).remove(eladamri);

        assertThat(gqs.hasKeyword(gd, elves, Keyword.FORESTWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.SHROUD)).isFalse();
    }
}
