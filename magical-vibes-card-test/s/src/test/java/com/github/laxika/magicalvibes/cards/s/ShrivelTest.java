package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shrivel.class, GrizzlyBears.class, FugitiveWizard.class})
class ShrivelTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-1 to every creature on both battlefields")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent theirs = findPermanent(player2, "Grizzly Bears");

        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(1);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness creatures")
    void killsOneToughnessCreatures() {
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1/1

        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectLaterCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getEffectivePower()).isEqualTo(1);
        assertThat(wizard.getEffectiveToughness()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Fugitive Wizard");
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Shrivels accumulate and kill two-toughness creatures")
    void repeatedCastsAccumulate() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolves without any creatures on the battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shrivel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The debuff still applies during the end step")
    void persistsThroughEndStep() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Shrivel(), "{1}{B}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }
}
