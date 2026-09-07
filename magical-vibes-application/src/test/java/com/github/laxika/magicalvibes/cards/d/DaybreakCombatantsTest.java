package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DaybreakCombatants.class, GrizzlyBears.class})
class DaybreakCombatantsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if the target creature leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can be cast without a creature to target")
    void castWithoutTarget() {
        harness.setHand(player1, List.of(new DaybreakCombatants()));
        addManaForDaybreakCombatants();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daybreak Combatants");
        assertThat(gd.stack).isEmpty();
    }

    private void castDaybreakCombatants(UUID targetId) {
        harness.setHand(player1, List.of(new DaybreakCombatants()));
        addManaForDaybreakCombatants();
        harness.castCreature(player1, 0, List.of(targetId));
    }

    private void addManaForDaybreakCombatants() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
