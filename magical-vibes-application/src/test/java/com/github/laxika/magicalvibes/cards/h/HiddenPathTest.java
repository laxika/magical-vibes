package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SavaenElves;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenPath.class, SavaenElves.class, Squire.class, Forest.class})
class HiddenPathTest extends BaseCardTest {

    @Test
    @DisplayName("Green creatures gain forestwalk on both battlefields")
    void grantsForestwalkToGreenCreatures() {
        harness.addToBattlefield(player1, new HiddenPath());
        harness.addToBattlefield(player1, new SavaenElves());
        harness.addToBattlefield(player2, new SavaenElves());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Savaen Elves"), Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Savaen Elves"), Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Non-green creatures do not gain forestwalk")
    void doesNotGrantForestwalkToNonGreenCreatures() {
        harness.addToBattlefield(player1, new HiddenPath());
        harness.addToBattlefield(player1, new Squire());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Squire"), Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Hidden Path does not grant forestwalk to a noncreature Forest")
    void doesNotGrantForestwalkToNoncreaturePermanent() {
        harness.addToBattlefield(player1, new HiddenPath());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, forest, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk prevents blocking while the defender controls a Forest")
    void forestwalkPreventsBlockingWithForest() {
        harness.addToBattlefield(player1, new HiddenPath());
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = declareCombat();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(blockAssignment(blocker))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk allows blocking when the defender controls no Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        harness.addToBattlefield(player1, new HiddenPath());

        Permanent blocker = declareCombat();

        gs.declareBlockers(gd, player2, List.of(blockAssignment(blocker)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Green creatures lose forestwalk when Hidden Path leaves")
    void forestwalkIsLostWhenSourceLeaves() {
        Permanent hiddenPath = harness.addToBattlefieldAndReturn(player1, new HiddenPath());
        harness.addToBattlefield(player2, new SavaenElves());

        Permanent elves = findPermanent(player2, "Savaen Elves");
        assertThat(gqs.hasKeyword(gd, elves, Keyword.FORESTWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(hiddenPath);

        assertThat(gqs.hasKeyword(gd, elves, Keyword.FORESTWALK)).isFalse();
    }

    private Permanent declareCombat() {
        Permanent attacker = addCreatureReady(player1, new SavaenElves());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Squire());

        prepareDeclareBlockers();

        return blocker;
    }

    private BlockerAssignment blockAssignment(Permanent blocker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = indexOfAttacker(player1);
        return new BlockerAssignment(blockerIdx, attackerIdx);
    }

    private int indexOfAttacker(Player player) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).isAttacking()) {
                return i;
            }
        }
        throw new IllegalStateException("No attacker found");
    }
}
