package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ControlWinCondition.class, Counterspell.class})
class ControlWinConditionTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness track the controller's turns taken")
    void powerAndToughnessTrackControllerTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        Permanent condition = addCreatureReady(player1, new ControlWinCondition());

        assertThat(gqs.getEffectivePower(gd, condition)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condition)).isEqualTo(3);

        gd.turnsTakenByPlayer.put(player1.getId(), 5);

        assertThat(gqs.getEffectivePower(gd, condition)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, condition)).isEqualTo(5);
    }

    @Test
    @DisplayName("Only the controller's turns count")
    void onlyControllersTurnsCount() {
        gd.turnsTakenByPlayer.put(player1.getId(), 2);
        gd.turnsTakenByPlayer.put(player2.getId(), 7);
        Permanent condition = addCreatureReady(player1, new ControlWinCondition());

        assertThat(gqs.getEffectivePower(gd, condition)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condition)).isEqualTo(2);
    }

    @Test
    @DisplayName("The creature spell cannot be countered")
    void creatureSpellCannotBeCountered() {
        ControlWinCondition condition = new ControlWinCondition();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(condition));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, condition.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Control Win Condition");
    }
}
