package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SkyshroudElf;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
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

@CardUsed({EladamriLordOfLeaves.class, Forest.class, SkyshroudElf.class, TrainedArmodon.class})
class EladamriLordOfLeavesTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elf creatures have forestwalk and shroud")
    void grantsToOwnElves() {
        harness.addToBattlefield(player1, new EladamriLordOfLeaves());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new SkyshroudElf());

        assertThat(gqs.hasKeyword(gd, elves, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Elves an opponent controls are affected too")
    void grantsToOpponentElves() {
        harness.addToBattlefield(player1, new EladamriLordOfLeaves());
        Permanent enemyElves = harness.addToBattlefieldAndReturn(player2, new SkyshroudElf());

        assertThat(gqs.hasKeyword(gd, enemyElves, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, enemyElves, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Eladamri grants nothing to itself")
    void doesNotGrantToItself() {
        Permanent eladamri = harness.addToBattlefieldAndReturn(player1, new EladamriLordOfLeaves());

        assertThat(gqs.hasKeyword(gd, eladamri, Keyword.FORESTWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, eladamri, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Non-Elf creatures are unaffected")
    void doesNotGrantToNonElves() {
        harness.addToBattlefield(player1, new EladamriLordOfLeaves());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        assertThat(gqs.hasKeyword(gd, nonElf, Keyword.FORESTWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonElf, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The grants end once Eladamri leaves the battlefield")
    void grantsEndWhenEladamriLeaves() {
        Permanent eladamri = harness.addToBattlefieldAndReturn(player1, new EladamriLordOfLeaves());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new SkyshroudElf());

        gd.playerBattlefields.get(player1.getId()).remove(eladamri);

        assertThat(gqs.hasKeyword(gd, elves, Keyword.FORESTWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk prevents blocking when the defending player controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        harness.addToBattlefield(player1, new EladamriLordOfLeaves());
        Permanent attacker = addAttackingElf();
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        harness.addToBattlefield(player2, new Forest());

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forestwalk does not prevent blocking without a Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        harness.addToBattlefield(player1, new EladamriLordOfLeaves());
        Permanent attacker = addAttackingElf();
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingElf() {
        Permanent attacker = addCreatureReady(player1, new SkyshroudElf());
        attacker.setAttacking(true);
        return attacker;
    }
}
