package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevilishValet.class, GrizzlyBears.class})
class DevilishValetTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles its power when another creature you control enters")
    void doublesPowerWhenAllyCreatureEnters() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valet)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubles its current power for each entering creature")
    void doublesCurrentPowerForEachAllyCreature() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void doesNotTriggerForOpponentCreature() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castGrizzlyBears(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power doubling wears off at end of turn")
    void powerDoublingWearsOffAtEndOfTurn() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        castGrizzlyBears(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(1);
    }

    private void castGrizzlyBears(Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
