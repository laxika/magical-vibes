package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RunedServitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hydrolash.class, RunedServitor.class})
class HydrolashTest extends BaseCardTest {

    private void giveSpell() {
        harness.setHand(player2, List.of(new Hydrolash()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Attacking creatures get -2/-0 and the caster draws a card")
    void weakensAttackersAndDraws() {
        Permanent attacker = addCreatureReady(player1, new RunedServitor());
        Permanent homeGuard = addCreatureReady(player2, new RunedServitor());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.setLibrary(player2, List.of(new RunedServitor()));
        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, homeGuard)).isEqualTo(2);
        harness.assertInHand(player2, "Runed Servitor");

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The penalty wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new RunedServitor());
        addCreatureReady(player2, new RunedServitor());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();
        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that attack after resolution are unaffected")
    void laterAttackersAreUnaffected() {
        Permanent second = addCreatureReady(player1, new RunedServitor());
        addCreatureReady(player2, new RunedServitor());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveSpell();
        harness.setLibrary(player2, List.of(new RunedServitor()));
        harness.castAndResolveInstant(player2, 0);

        harness.assertInHand(player2, "Runed Servitor");
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(second.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("The caster's attacking creatures are also affected and penalties stack")
    void weakensOwnAttackersAndStacks() {
        Permanent first = addCreatureReady(player2, new RunedServitor());
        Permanent second = addCreatureReady(player2, new RunedServitor());
        addCreatureReady(player1, new RunedServitor());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0, 1)));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        giveSpell();
        harness.setLibrary(player2, List.of(new RunedServitor(), new RunedServitor()));
        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectivePower(gd, second)).isZero();

        giveSpell();
        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }
}
