package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompelledDuel.class, GrizzlyBears.class})
class CompelledDuelTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +3/+3 and must be blocked if able")
    void boostsAndForcesBlock() {
        castCompelledDuelOnBears();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Boost and must-be-blocked requirement wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        castCompelledDuelOnBears();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Compelled Duel fizzles if its target leaves the battlefield")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompelledDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Compelled Duel");
    }

    private void castCompelledDuelOnBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CompelledDuel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
