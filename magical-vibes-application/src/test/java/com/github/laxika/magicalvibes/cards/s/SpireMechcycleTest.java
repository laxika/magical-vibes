package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloryheathLynx;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.v.VoyagerGlidecar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Spire Mechcycle")
@CardUsed({SpireMechcycle.class, VoyagerGlidecar.class, GrizzlyBears.class,
        GloryheathLynx.class, TurnToFrog.class})
class SpireMechcycleTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust taps another Mount or Vehicle, animates permanently, and counts other permanents")
    void exhaustTapsAnotherPermanentAndAnimatesPermanently() {
        Permanent spireMechcycle = addReady(new SpireMechcycle());
        Permanent firstVehicle = addReady(new VoyagerGlidecar());
        Permanent secondVehicle = addReady(new VoyagerGlidecar());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstVehicle.getId());
        harness.passBothPriorities();

        assertThat(firstVehicle.isTapped()).isTrue();
        assertThat(secondVehicle.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, spireMechcycle)).isTrue();
        assertThat(gqs.isArtifact(spireMechcycle)).isTrue();
        assertThat(spireMechcycle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spireMechcycle)).isTrue();
    }

    @Test
    @DisplayName("Crew 2 animates Spire Mechcycle until end of turn")
    void crewAnimatesUntilEndOfTurn() {
        Permanent spireMechcycle = addReady(new SpireMechcycle());
        Permanent firstCrew = addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spireMechcycle)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spireMechcycle)).isFalse();
    }

    @Test
    @DisplayName("Exhaust cannot be activated without another untapped Mount or Vehicle")
    void exhaustRequiresAnotherUntappedMountOrVehicle() {
        addReady(new SpireMechcycle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReady(new SpireMechcycle());
        addReady(new VoyagerGlidecar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void exhaustCanTapASummoningSickMount() {
        Permanent spire = harness.addToBattlefieldAndReturn(player1, new SpireMechcycle());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new GloryheathLynx());
        assertThat(mount.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mount.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, spire)).isTrue();
        assertThat(spire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tappedVehicleCannotPayExhaustCost() {
        addReady(new SpireMechcycle());
        addReady(new VoyagerGlidecar()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsVehicleCannotPayExhaustCost() {
        addReady(new SpireMechcycle());
        harness.addToBattlefield(player2, new VoyagerGlidecar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exhaustCountsAtResolutionAndIgnoresOpponentsVehicles() {
        Permanent spire = addReady(new SpireMechcycle());
        addReady(new VoyagerGlidecar());
        harness.addToBattlefield(player2, new VoyagerGlidecar());

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new GloryheathLynx());
        harness.passBothPriorities();

        assertThat(spire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void crewingAfterExhaustDoesNotEndPermanentAnimation() {
        Permanent spire = addReady(new SpireMechcycle());
        addReady(new VoyagerGlidecar());
        addReady(new GloryheathLynx());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, spire)).isTrue();
        assertThat(spire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void exhaustDoesNotOverwriteAnExistingBasePowerToughnessEffect() {
        Permanent spire = addReady(new SpireMechcycle());
        addReady(new VoyagerGlidecar());
        addReady(new GloryheathLynx());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, spire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spire)).isEqualTo(3);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
