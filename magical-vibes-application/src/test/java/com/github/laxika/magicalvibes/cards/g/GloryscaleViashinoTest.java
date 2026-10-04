package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BehemothSledge;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloryscaleViashino.class, GrizzlyBears.class, MycosynthLattice.class, BehemothSledge.class})
class GloryscaleViashinoTest extends BaseCardTest {

    private Permanent addViashino() {
        Permanent viashino = harness.addToBattlefieldAndReturn(player1, new GloryscaleViashino());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return viashino;
    }

    @Test
    @DisplayName("Gets +3/+3 when you cast a multicolored spell")
    void pumpsWhenMulticoloredSpellCast() {
        Permanent viashino = addViashino();

        // A second Gloryscale Viashino ({1}{R}{G}{W}) is a multicolored spell.
        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");

        // Cast trigger sits on the stack above the creature spell.
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(viashino.getPowerModifier()).isEqualTo(3);
        assertThat(viashino.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not pump when you cast a monocolored spell")
    void noPumpForMonocoloredSpell() {
        Permanent viashino = addViashino();

        // Grizzly Bears is a monocolored (green) spell.
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        // No cast trigger — only the creature spell is on the stack.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(viashino.getPowerModifier()).isEqualTo(0);
        assertThat(viashino.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent viashino = addViashino();

        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(viashino.getPowerModifier()).isEqualTo(3);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(viashino.getPowerModifier()).isEqualTo(0);
        assertThat(viashino.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each multicolored spell gives a separate cumulative boost")
    void multipleCastsAccumulateBoosts() {
        Permanent viashino = addViashino();
        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        resolveAllTriggers();
        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThat(second.getPowerModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();

        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");
        resolveAllTriggers();

        assertThat(viashino.getPowerModifier()).isEqualTo(6);
        assertThat(viashino.getToughnessModifier()).isEqualTo(6);
        assertThat(second.getPowerModifier()).isEqualTo(3);
        assertThat(second.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's multicolored spell does not trigger the boost")
    void opponentCastDoesNotBoost() {
        Permanent viashino = addViashino();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GloryscaleViashino(), "{1}{R}{G}{W}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(viashino.getPowerModifier()).isZero();
        assertThat(viashino.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Casting Gloryscale Viashino does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent viashino = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(viashino.getPowerModifier()).isZero();
        assertThat(viashino.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A two-color noncreature spell also triggers the boost")
    void twoColorArtifactSpellBoosts() {
        Permanent viashino = addViashino();
        harness.castFromHand(player1, new BehemothSledge(), "{1}{G}{W}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(viashino.getPowerModifier()).isEqualTo(3);
        assertThat(viashino.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell made colorless by Mycosynth Lattice does not trigger the boost")
    void colorlessSpellUnderLatticeDoesNotBoost() {
        Permanent viashino = addViashino();
        harness.addToBattlefield(player2, new MycosynthLattice());
        harness.castFromHand(player1, new GloryscaleViashino(), "{1}{R}{G}{W}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(viashino.getPowerModifier()).isZero();
        assertThat(viashino.getToughnessModifier()).isZero();
    }
}
