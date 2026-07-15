package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoldmeadowDodgerTest extends BaseCardTest {

    @Test
    @DisplayName("Goldmeadow Dodger cannot be blocked by a creature with power 4 or greater")
    void cannotBeBlockedByPower4OrGreater() {
        Permanent dodger = attackingDodger();
        gd.playerBattlefields.get(player1.getId()).add(dodger);

        Permanent elemental = new Permanent(new AirElemental()); // 4/4
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by");
    }

    @Test
    @DisplayName("Goldmeadow Dodger can be blocked by a creature with power 3 or less")
    void canBeBlockedByPower3OrLess() {
        Permanent dodger = attackingDodger();
        gd.playerBattlefields.get(player1.getId()).add(dodger);

        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Helpers =====

    private Permanent attackingDodger() {
        Permanent dodger = new Permanent(new GoldmeadowDodger());
        dodger.setSummoningSick(false);
        dodger.setAttacking(true);
        return dodger;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
