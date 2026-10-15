package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeldfastWingsmith.class, PrakhataPillarBug.class, Panharmonicon.class, MycosynthLattice.class})
class WeldfastWingsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Flying is granted only after the artifact-entry trigger resolves")
    void flyingWaitsForTriggerResolution() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.castFromHand(player1, new PrakhataPillarBug(), "{3}");

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A noncreature artifact entering also grants flying")
    void noncreatureArtifactTriggers() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.castFromHand(player1, new Panharmonicon(), "{4}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A nonartifact creature entering does not grant flying")
    void nonartifactCreatureDoesNotTrigger() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.castFromHand(player1, new WeldfastWingsmith(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wingsmith triggers for its own entry when it enters as an artifact")
    void enteringAsArtifactTriggersOwnAbility() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.castFromHand(player1, new WeldfastWingsmith(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        Permanent wingsmith = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WeldfastWingsmith)
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An artifact you control entering gives Weldfast Wingsmith flying until end of turn")
    void allyArtifactEnterGrantsFlying() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.castFromHand(player1, new PrakhataPillarBug(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying granted by the artifact trigger wears off at end of turn")
    void flyingWearsOffAtCleanup() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.castFromHand(player1, new PrakhataPillarBug(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not grant flying")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new PrakhataPillarBug(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();
    }
}
