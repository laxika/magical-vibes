package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProwlingNightstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Prowling Nightstalker cannot be blocked by a non-black creature")
    void cannotBeBlockedByNonBlackCreature() {
        Permanent nightstalker = attackingNightstalker();
        gd.playerBattlefields.get(player1.getId()).add(nightstalker);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("Prowling Nightstalker can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        Permanent nightstalker = attackingNightstalker();
        gd.playerBattlefields.get(player1.getId()).add(nightstalker);

        Permanent imp = new Permanent(new DuskImp());
        imp.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(imp);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent attackingNightstalker() {
        Permanent nightstalker = new Permanent(new ProwlingNightstalker());
        nightstalker.setSummoningSick(false);
        nightstalker.setAttacking(true);
        return nightstalker;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
