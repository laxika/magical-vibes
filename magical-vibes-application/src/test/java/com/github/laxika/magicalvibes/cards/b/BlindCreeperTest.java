package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindCreeper.class, ConjurersBauble.class})
class BlindCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell gives Blind Creeper -1/-1")
    void controllerCastingSpellShrinksBlindCreeper() {
        harness.addToBattlefield(player1, new BlindCreeper());
        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blindCreeper())).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent casting a spell gives Blind Creeper -1/-1")
    void opponentCastingSpellShrinksBlindCreeper() {
        harness.addToBattlefield(player1, new BlindCreeper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new ConjurersBauble(), "{1}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blindCreeper())).isEqualTo(2);
    }

    @Test
    @DisplayName("Blind Creeper's spell-cast penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BlindCreeper());
        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blindCreeper())).isEqualTo(3);
    }

    @Test
    @DisplayName("Each spell cast gives Blind Creeper another -1/-1 until end of turn")
    void eachSpellCastAddsAnotherPenalty() {
        harness.addToBattlefield(player1, new BlindCreeper());

        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ConjurersBauble(), "{1}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blindCreeper())).isEqualTo(1);
    }

    private Permanent blindCreeper() {
        return findPermanent(player1, "Blind Creeper");
    }
}
