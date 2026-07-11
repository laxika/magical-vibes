package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreadChargeTest extends BaseCardTest {

    @Test
    @DisplayName("A black creature you control can't be blocked by a non-black creature")
    void blackCreatureCannotBeBlockedByNonBlack() {
        Permanent attacker = addCreatureReady(player1, new ScatheZombies());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        resolveDreadCharge();

        prepareDeclareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("A black creature you control can still be blocked by a black creature")
    void blackCreatureCanBeBlockedByBlack() {
        Permanent attacker = addCreatureReady(player1, new ScatheZombies());
        addCreatureReady(player2, new WalkingCorpse());
        resolveDreadCharge();

        prepareDeclareBlockers(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("A non-black creature you control is unaffected and can be blocked normally")
    void nonBlackCreatureIsUnaffected() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        resolveDreadCharge();

        prepareDeclareBlockers(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private void resolveDreadCharge() {
        harness.setHand(player1, List.of(new DreadCharge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void prepareDeclareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
