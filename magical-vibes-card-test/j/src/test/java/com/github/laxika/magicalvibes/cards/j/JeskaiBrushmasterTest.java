package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JeskaiBrushmaster.class, GrizzlyBears.class, Shock.class})
class JeskaiBrushmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell gives Jeskai Brushmaster +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent brushmaster = addBrushmaster();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brushmaster)).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent brushmaster = addBrushmaster();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, brushmaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brushmaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Double strike deals combat damage twice")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);

        Permanent brushmaster = harness.addToBattlefieldAndReturn(player1, new JeskaiBrushmaster());
        brushmaster.setSummoningSick(false);
        brushmaster.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Each noncreature spell triggers prowess separately")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent brushmaster = addBrushmaster();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
        }

        assertThat(gqs.getEffectivePower(gd, brushmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brushmaster)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent brushmaster = addBrushmaster();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brushmaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brushmaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("The prowess boost expires during cleanup")
    void prowessBoostExpires() {
        Permanent brushmaster = addBrushmaster();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, brushmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brushmaster)).isEqualTo(5);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, brushmaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, brushmaster)).isEqualTo(4);
    }

    private Permanent addBrushmaster() {
        Permanent brushmaster = harness.addToBattlefieldAndReturn(player1, new JeskaiBrushmaster());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return brushmaster;
    }
}
