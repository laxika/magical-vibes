package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, Nausea.class, SuntailHawk.class, Swamp.class})
class NauseaTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-1 to every creature on both battlefields")
    void debuffsAllCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not affect creatures entering later in the turn")
    void doesNotAffectCreaturesEnteringLater() {
        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kills 1-toughness creatures")
    void killsOneToughnessCreatures() {
        harness.addToBattlefield(player2, new SuntailHawk()); // 1/1

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not affect noncreature permanents")
    void doesNotAffectNoncreaturePermanents() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Swamp");
        assertThat(gqs.getEffectivePower(gd, swamp)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isZero();
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
