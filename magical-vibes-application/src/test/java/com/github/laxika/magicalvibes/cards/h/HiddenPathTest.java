package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenPath.class, GrizzlyBears.class, EliteVanguard.class, Forest.class})
class HiddenPathTest extends BaseCardTest {

    @Test
    @DisplayName("Green creatures gain forestwalk on both battlefields")
    void grantsForestwalkToGreenCreatures() {
        harness.addToBattlefield(player1, new HiddenPath());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Non-green creatures do not gain forestwalk")
    void doesNotGrantForestwalkToNonGreenCreatures() {
        harness.addToBattlefield(player1, new HiddenPath());
        harness.addToBattlefield(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Elite Vanguard"), Keyword.FORESTWALK)).isFalse();
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
        harness.addToBattlefield(player1, new HiddenPath());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Hidden Path"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    private Permanent declareCombat() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new EliteVanguard());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
