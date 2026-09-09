package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedDevils.class, GrizzlyBears.class, Shock.class})
class FrenziedDevilsTest extends BaseCardTest {

    private Permanent addFrenziedDevils() {
        Permanent devils = harness.addToBattlefieldAndReturn(player1, new FrenziedDevils());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return devils;
    }

    @Test
    @DisplayName("Frenzied Devils gets +2/+2 when its controller casts a noncreature spell")
    void noncreatureSpellPumps() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(5);
    }

    @Test
    @DisplayName("Frenzied Devils does not trigger when its controller casts a creature spell")
    void creatureSpellDoesNotPump() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(3);
    }

    @Test
    @DisplayName("Frenzied Devils's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent devils = addFrenziedDevils();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, devils)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, devils)).isEqualTo(3);
    }
}
