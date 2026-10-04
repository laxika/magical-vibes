package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellSwarm.class, GrizzlyBears.class})
class HellSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-0 to every creature on both battlefields")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castHellSwarm();

        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent theirs = findPermanent(player2, "Grizzly Bears");
        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castHellSwarm();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect creatures that enter after it resolves")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        Permanent existing = addCreatureReady(player1, new GrizzlyBears());

        castHellSwarm();

        Permanent later = addCreatureReady(player2, new GrizzlyBears());

        assertThat(existing.getEffectivePower()).isEqualTo(1);
        assertThat(existing.getEffectiveToughness()).isEqualTo(2);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects creatures that enter while Hell Swarm is on the stack")
    void affectsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new HellSwarm(), "{B}");
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves without creatures and does not affect later arrivals")
    void resolvesOnEmptyBattlefield() {
        castHellSwarm();

        harness.assertInGraveyard(player1, "Hell Swarm");
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Hell Swarms stack and can reduce power below zero")
    void multipleCastsCanGiveNegativePower() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castHellSwarm();
        castHellSwarm();
        castHellSwarm();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(-1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castHellSwarm() {
        harness.castFromHand(player1, new HellSwarm(), "{B}");
        harness.passBothPriorities();
    }
}
