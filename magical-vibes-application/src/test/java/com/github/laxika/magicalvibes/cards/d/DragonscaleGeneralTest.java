package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonscaleGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Bolsters by the number of tapped creatures you control")
    void bolstersByTappedCreatureCount() {
        harness.addToBattlefield(player1, new DragonscaleGeneral());
        Permanent leastToughCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largerCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        leastToughCreature.tap();
        largerCreature.tap();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Uses the tapped creature count when choosing among tied creatures")
    void choosesAmongTiedLeastToughCreatures() {
        harness.addToBattlefield(player1, new DragonscaleGeneral());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 1));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when no creatures are tapped")
    void doesNothingWithoutTappedCreatures() {
        harness.addToBattlefield(player1, new DragonscaleGeneral());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
