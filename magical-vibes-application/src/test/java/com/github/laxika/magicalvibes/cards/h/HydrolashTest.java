package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HydrolashTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player2, List.of(new Hydrolash()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Attacking creatures get -2/-0 and the caster draws a card")
    void weakensAttackersAndDraws() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent homeGuard = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, homeGuard)).isEqualTo(2);
        harness.assertInHand(player2, "Grizzly Bears");

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The penalty wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that attack after resolution are unaffected")
    void laterAttackersAreUnaffected() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }
}
