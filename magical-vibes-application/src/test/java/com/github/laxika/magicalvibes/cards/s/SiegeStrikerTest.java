package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SiegeStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts to tap any number of untapped creatures")
    void attackTriggerPromptsForUntappedCreatures() {
        addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Siege Striker gets +1/+1 for each creature tapped this way")
    void boostsForEachCreatureTapped() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(striker.getPowerModifier()).isEqualTo(2);
        assertThat(striker.getToughnessModifier()).isEqualTo(2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing no creatures leaves Siege Striker unboosted")
    void choosingNoCreaturesDoesNothing() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(striker.getPowerModifier()).isZero();
        assertThat(striker.getToughnessModifier()).isZero();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent striker = addCreatureReady(player1, new SiegeStriker());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(striker.getPowerModifier()).isEqualTo(1);
        assertThat(striker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(striker.getPowerModifier()).isZero();
        assertThat(striker.getToughnessModifier()).isZero();
    }
}
