package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisceridDroneTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature and a Swamp destroys target nonartifact creature")
    void swampAbilityDestroysNonartifactCreature() {
        addCreatureReady(player1, new VisceridDrone());
        UUID fodderId = addCreatureReady(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player1, new Swamp());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        // Only the creature slot prompts — the lone Swamp is the sole legal pick for its slot.
        harness.activateAbility(player1, 0, 0, null, victim.getId());
        harness.handlePermanentChosen(player1, fodderId);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swamp");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The source can be sacrificed as the creature cost")
    void sourceCanBeSacrificedAsCreatureCost() {
        Permanent source = addCreatureReady(player1, new VisceridDrone());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, victim.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.handlePermanentChosen(player1, swamp.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viscerid Drone");
        harness.assertInGraveyard(player1, "Swamp");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Swamp ability can't target an artifact creature")
    void swampAbilityCannotTargetArtifactCreature() {
        addCreatureReady(player1, new VisceridDrone());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Swamp());
        Permanent artifactCreature = addCreatureReady(player2, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a creature and a snow Swamp destroys any target creature, artifact included")
    void snowSwampAbilityDestroysArtifactCreature() {
        addCreatureReady(player1, new VisceridDrone());
        UUID fodderId = addCreatureReady(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        Permanent victim = addCreatureReady(player2, new Ornithopter());

        harness.activateAbility(player1, 0, 1, null, victim.getId());
        harness.handlePermanentChosen(player1, fodderId);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("The snow ability can't be activated with only a nonsnow Swamp")
    void snowSwampAbilityRequiresSnowSwamp() {
        addCreatureReady(player1, new VisceridDrone());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Swamp());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
