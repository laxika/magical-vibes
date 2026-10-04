package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities(); // advance through cleanup

        assertThat(mine.getPowerModifier()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"0, 2, 0", "1, 0, 2"})
    @DisplayName("Each mode boosts all own creatures present at resolution")
    void boostsCreaturesEnteringBeforeResolution(int mode, int power, int toughness) {
        Permanent first = addCreatureReady(player1, new AshcoatBear());
        Permanent second = addCreatureReady(player1, new AshcoatBear());
        Permanent opponent = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, mode, List.of());
        Permanent beforeResolution = addCreatureReady(player1, new AshcoatBear());
        harness.passBothPriorities();

        for (Permanent creature : List.of(first, second, beforeResolution)) {
            assertThat(creature.getPowerModifier()).isEqualTo(power);
            assertThat(creature.getToughnessModifier()).isEqualTo(toughness);
        }
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();

        Permanent afterResolution = addCreatureReady(player1, new AshcoatBear());
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"0", "1"})
    @DisplayName("Either mode can resolve with no creatures controlled")
    void resolvesWithNoOwnCreatures(int mode) {
        Permanent opponent = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fortify");
        assertThat(gd.stack).isEmpty();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Toughness boost lasts through the end step and expires at cleanup")
    void toughnessBoostExpiresAtCleanup() {
        Permanent mine = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new Fortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(mine.getToughnessModifier()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isZero();
        assertThat(mine.getToughnessModifier()).isZero();
    }
}
