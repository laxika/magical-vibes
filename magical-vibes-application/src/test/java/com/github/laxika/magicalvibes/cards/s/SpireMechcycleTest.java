package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoyagerGlidecar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Spire Mechcycle")
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

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
