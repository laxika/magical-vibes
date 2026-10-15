package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitheringCurse.class, GrizzlyBears.class, HillGiant.class, DrudgeSkeletons.class})
class WitheringCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Without life gained, all creatures get -2/-2 (kills the 2/2, spares the 3/3)")
    void withoutLifeGainAppliesMinusTwoMinusTwo() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("If you gained life this turn, destroys all creatures instead (kills the 3/3 too)")
    void withLifeGainDestroysAllCreatures() {
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void opponentLifeGainDoesNotEnableInfusionAndBothPlayersCreaturesAreReduced() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1);
        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    void lifeGainedAfterCastingEnablesInfusionEvenAfterLifeIsLost() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);

        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, "test");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void reductionExpiresAndDoesNotAffectCreaturesEnteringAfterResolution() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void infusionAllowsRegenerationAndDoesNotAlsoReduceToughness() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        assertThat(skeletons.isTapped()).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }

    @Test
    void regenerationCannotSaveCreatureFromToughnessReduction() {
        harness.addToBattlefield(player1, new DrudgeSkeletons());
        harness.setHand(player1, List.of(new WitheringCurse()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
        harness.assertInGraveyard(player1, "Drudge Skeletons");
    }
}
