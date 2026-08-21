package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EagerFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts Eager First-Year until end of turn")
    void castingInstantBoostsEagerFirstYear() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(firstYear.getEffectivePower()).isEqualTo(3);
        assertThat(firstYear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copying an instant boosts Eager First-Year")
    void copyingInstantBoostsEagerFirstYear() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, firstYear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstYear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Eager First-Year")
    void castingCreatureDoesNotBoostEagerFirstYear() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(firstYear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(firstYear.getEffectivePower()).isEqualTo(2);
        assertThat(firstYear.getEffectiveToughness()).isEqualTo(2);
    }
}
