package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyTheaterStrix.class, GrizzlyBears.class, Shock.class})
class SkyTheaterStrixTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when its controller casts a noncreature spell")
    void pumpsForNoncreatureSpell() {
        Permanent strix = addStrix();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, strix)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, strix)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a creature spell")
    void doesNotPumpForCreatureSpell() {
        Permanent strix = addStrix();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, strix)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, strix)).isEqualTo(2);
    }

    @Test
    @DisplayName("The temporary power boost wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent strix = addStrix();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, strix)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, strix)).isEqualTo(2);
    }

    private Permanent addStrix() {
        Permanent strix = harness.addToBattlefieldAndReturn(player1, new SkyTheaterStrix());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return strix;
    }
}
