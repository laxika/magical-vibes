package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlindCreeperTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell gives Blind Creeper -1/-1")
    void controllerCastingSpellShrinksBlindCreeper() {
        harness.addToBattlefield(player1, new BlindCreeper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blindCreeper())).isEqualTo(2);
    }

    @Test
    @DisplayName("Blind Creeper's spell-cast penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BlindCreeper());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blindCreeper())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blindCreeper())).isEqualTo(3);
    }

    private Permanent blindCreeper() {
        return findPermanent(player1, "Blind Creeper");
    }
}
