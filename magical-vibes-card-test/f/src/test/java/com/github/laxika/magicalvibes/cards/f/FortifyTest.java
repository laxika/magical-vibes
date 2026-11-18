package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

        assertThat(PermanentGrants.powerBoostOf(gd, mine)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mine)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, theirs)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, theirs)).isZero();
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

        assertThat(PermanentGrants.powerBoostOf(gd, mine)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, mine)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, theirs)).isZero();
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

        assertThat(PermanentGrants.powerBoostOf(gd, mine)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, later)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, later)).isZero();
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
        assertThat(PermanentGrants.powerBoostOf(gd, mine)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities(); // advance through cleanup

        assertThat(PermanentGrants.powerBoostOf(gd, mine)).isZero();
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
            assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(power);
            assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(toughness);
        }
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();

        Permanent afterResolution = addCreatureReady(player1, new AshcoatBear());
        assertThat(PermanentGrants.powerBoostOf(gd, afterResolution)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, afterResolution)).isZero();
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
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();
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
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(PermanentGrants.toughnessBoostOf(gd, mine)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, mine)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, mine)).isZero();
    }
}
