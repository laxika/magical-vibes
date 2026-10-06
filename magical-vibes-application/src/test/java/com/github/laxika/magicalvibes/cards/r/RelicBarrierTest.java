package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicBarrier.class, BronzeHorse.class, AzureDrake.class})
class RelicBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Tap target artifact taps the chosen artifact")
    void tapAbilityTapsTargetArtifact() {
        Permanent barrier = addReadyBarrier(player1);
        Permanent artifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(barrier.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can target an artifact you control")
    void canTargetOwnArtifact() {
        addReadyBarrier(player1);
        Permanent artifact = addArtifact(player1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can target an artifact creature")
    void canTargetArtifactCreature() {
        addReadyBarrier(player1);
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new BronzeHorse());

        harness.activateAbility(player1, 0, null, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(artifactCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can target an already-tapped artifact creature")
    void canTargetAlreadyTappedArtifactCreature() {
        addReadyBarrier(player1);
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new BronzeHorse());
        artifactCreature.tap();

        harness.activateAbility(player1, 0, null, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(artifactCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        addReadyBarrier(player1);
        Permanent creature = addCreatureReady(player2, new AzureDrake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature Relic Barrier can activate on the turn it enters")
    void canActivateImmediately() {
        Permanent barrier = harness.addToBattlefieldAndReturn(player1, new RelicBarrier());
        Permanent artifact = addArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(barrier.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Relic Barrier can target itself despite tapping to pay its cost")
    void canTargetItself() {
        Permanent barrier = addReadyBarrier(player1);

        harness.activateAbility(player1, 0, null, barrier.getId());
        harness.passBothPriorities();

        assertThat(barrier.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Relic Barrier cannot pay its tap cost")
    void cannotActivateWhileTapped() {
        Permanent barrier = addReadyBarrier(player1);
        Permanent artifact = addArtifact(player2);
        barrier.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBarrier(Player player) {
        return addCreatureReady(player, new RelicBarrier());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new RelicBarrier());
    }
}
