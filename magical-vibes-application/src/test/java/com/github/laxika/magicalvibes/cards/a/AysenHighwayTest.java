package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AysenHighwayTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures gain plainswalk, including the opponent's")
    void grantsPlainswalkToWhiteCreatures() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Elite Vanguard"), Keyword.PLAINSWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Elite Vanguard"), Keyword.PLAINSWALK)).isTrue();
    }

    @Test
    @DisplayName("Non-white creatures do not gain plainswalk")
    void doesNotGrantToNonWhiteCreatures() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.PLAINSWALK)).isFalse();
    }

    @Test
    @DisplayName("Grant is removed when Aysen Highway leaves the battlefield")
    void grantRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player1, new EliteVanguard());

        Permanent vanguard = findPermanent(player1, "Elite Vanguard");
        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.PLAINSWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Aysen Highway"));

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.PLAINSWALK)).isFalse();
    }

    @Test
    @DisplayName("White creature can't be blocked when defender controls a Plains")
    void plainswalkPreventsBlockingWithPlains() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player2, new Plains());

        Permanent blocker = declareCombat();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(blockAssignment(blocker))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("White creature can be blocked when defender controls no Plains")
    void plainswalkAllowsBlockingWithoutPlains() {
        harness.addToBattlefield(player1, new AysenHighway());

        Permanent blocker = declareCombat();

        gs.declareBlockers(gd, player2, List.of(blockAssignment(blocker)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent declareCombat() {
        Permanent attacker = new Permanent(new EliteVanguard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
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
