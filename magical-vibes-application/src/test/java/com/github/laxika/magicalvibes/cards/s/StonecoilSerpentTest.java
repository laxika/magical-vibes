package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloryscaleViashino;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonecoilSerpent.class, GloryscaleViashino.class})
class StonecoilSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXPlusOneCounters() {
        harness.setHand(player1, List.of(new StonecoilSerpent()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Stonecoil Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 causes the 0/0 Stonecoil Serpent to die")
    void zeroCountersDiesToStateBasedActions() {
        harness.setHand(player1, List.of(new StonecoilSerpent()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stonecoil Serpent");
    }

    @Test
    @DisplayName("Stonecoil Serpent cannot be blocked by a multicolored creature")
    void cannotBeBlockedByMulticoloredCreature() {
        Permanent serpent = new Permanent(new StonecoilSerpent());
        serpent.setSummoningSick(false);
        serpent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        serpent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        Permanent blocker = addCreatureReady(player2, new GloryscaleViashino());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
