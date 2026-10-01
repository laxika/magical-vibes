package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fortify.class, AshcoatBear.class})
class FortifyTest extends BaseCardTest {

    // Modes: 0 = +2/+0, 1 = +0/+2

    @Test
    @DisplayName("Mode 0: creatures you control get +2/+0, opponent's creatures unaffected")
    void powerModeBoostsOnlyOwnCreatures() {
        Permanent mine = addCreatureReady(player1, new AshcoatBear());
        Permanent theirs = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(2);
        assertThat(mine.getToughnessModifier()).isZero();
        assertThat(theirs.getPowerModifier()).isZero();
        assertThat(theirs.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Mode 1: creatures you control get +0/+2")
    void toughnessModeBoostsOwnCreatures() {
        Permanent mine = addCreatureReady(player1, new AshcoatBear());
        Permanent theirs = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isZero();
        assertThat(mine.getToughnessModifier()).isEqualTo(2);
        assertThat(theirs.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not boost creatures that enter after the spell resolves")
    void doesNotBoostLaterCreatures() {
        Permanent mine = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        Permanent later = addCreatureReady(player1, new AshcoatBear());

        assertThat(mine.getPowerModifier()).isEqualTo(2);
        assertThat(later.getPowerModifier()).isZero();
        assertThat(later.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mine = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(mine.getPowerModifier()).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance through cleanup

        assertThat(mine.getPowerModifier()).isZero();
    }
}
