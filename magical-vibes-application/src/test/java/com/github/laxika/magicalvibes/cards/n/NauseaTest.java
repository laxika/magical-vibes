package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FugitiveWizard.class, GrizzlyBears.class, Nausea.class})
class NauseaTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-1 to every creature on both battlefields")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent theirs = findPermanent(player2, "Grizzly Bears");

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
        harness.addToBattlefield(player2, new FugitiveWizard()); // 1/1

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        harness.castFromHand(player1, new Nausea(), "{1}{B}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
