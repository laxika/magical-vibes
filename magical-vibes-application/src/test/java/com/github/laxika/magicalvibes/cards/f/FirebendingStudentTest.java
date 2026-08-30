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

@CardUsed({FirebendingStudent.class, GrizzlyBears.class, Shock.class})
class FirebendingStudentTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess increases firebending mana based on the student's power")
    void firebendingUsesEffectivePower() {
        Permanent student = addReadyStudent();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, student)).isEqualTo(2);

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Prowess does not trigger for a creature spell")
    void creatureSpellDoesNotPump() {
        Permanent student = addReadyStudent();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, student)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, student)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess wears off at the end of the turn")
    void prowessWearsOffAtEndOfTurn() {
        Permanent student = addReadyStudent();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, student)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, student)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, student)).isEqualTo(2);
    }

    private Permanent addReadyStudent() {
        return addCreatureReady(player1, new FirebendingStudent());
    }
}
