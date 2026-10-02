package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CopperhornScout;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.m.MyrGalvanizer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({BellowingTanglewurm.class, CopperhornScout.class, MoriokReaver.class, MyrGalvanizer.class})
class BellowingTanglewurmTest extends BaseCardTest {

    @Test
    @DisplayName("Own green creature gains intimidate")
    void ownGreenCreatureGainsIntimidate() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CopperhornScout());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Bellowing Tanglewurm cannot be blocked by a nonartifact black creature")
    void innateIntimidateRestrictsBlocking() {
        Permanent wurm = addCreatureReady(player1, new BellowingTanglewurm());
        addCreatureReady(player2, new MoriokReaver());
        wurm.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    @DisplayName("Does not grant intimidate to own non-green creature")
    void doesNotGrantToNonGreenCreature() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());
        assertThat(gqs.hasKeyword(gd, giant, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant intimidate to opponent's green creature")
    void doesNotGrantToOpponentGreenCreature() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CopperhornScout());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Intimidate is lost when Bellowing Tanglewurm leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CopperhornScout());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();

        // Remove the lord
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Bellowing Tanglewurm"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    void grantedIntimidateRejectsNonartifactCreatureOfAnotherColor() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        addCreatureReady(player2, new MoriokReaver());
        scout.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void grantedIntimidateAllowsGreenBlocker() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent blocker = addCreatureReady(player2, new CopperhornScout());
        scout.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void grantedIntimidateAllowsColorlessArtifactBlocker() {
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent blocker = addCreatureReady(player2, new MyrGalvanizer());
        scout.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void anotherTanglewurmKeepsGrantActiveWhenOneLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BellowingTanglewurm());
        harness.addToBattlefield(player1, new BellowingTanglewurm());
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new CopperhornScout());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, scout, Keyword.INTIMIDATE)).isTrue();
    }
}
