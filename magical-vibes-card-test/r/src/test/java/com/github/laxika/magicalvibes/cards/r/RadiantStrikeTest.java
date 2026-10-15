package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadiantStrike.class, FountainOfYouth.class, GrizzlyBears.class})
class RadiantStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and the spell's controller gains 3 life")
    void destroysArtifactAndGainsLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLife(player1, 10);

        cast(artifact);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Destroys a tapped creature and the spell's controller gains 3 life")
    void destroysTappedCreatureAndGainsLife() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        harness.setLife(player1, 10);

        cast(creature);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent creature = addCreatureReady(player2);
        prepareToCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be an artifact or a tapped creature");
    }

    @Test
    @DisplayName("An untapped creature target becomes illegal and no life is gained")
    void untappingCreaturePreventsResolutionAndLifeGain() {
        Permanent creature = addCreatureReady(player2);
        creature.tap();
        harness.setLife(player1, 10);
        prepareToCast();
        harness.castInstant(player1, 0, creature.getId());

        creature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Radiant Strike");
    }

    @Test
    @DisplayName("An artifact remains a legal target when it becomes untapped")
    void untappingArtifactDoesNotPreventResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        artifact.tap();
        harness.setLife(player1, 10);
        prepareToCast();
        harness.castInstant(player1, 0, artifact.getId());

        artifact.untap();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can destroy its controller's own artifact")
    void canTargetOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLife(player1, 10);

        cast(artifact);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void cast(Permanent target) {
        prepareToCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new RadiantStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
