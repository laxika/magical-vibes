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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonscaleGeneral.class, GrizzlyBears.class, HillGiant.class})
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
                        CounterType.PLUS_ONE_PLUS_ONE, 1, true));

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

    @Test
    @DisplayName("Counts the General itself and ignores opposing tapped creatures")
    void countsItselfButNotOpposingCreatures() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new DragonscaleGeneral());
        Permanent opposingGeneral = harness.addToBattlefieldAndReturn(player2, new DragonscaleGeneral());
        general.tap();
        opposingGeneral.tap();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingGeneral.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new DragonscaleGeneral());
        general.tap();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Triggers even with zero tapped creatures and counts a creature tapped before resolution")
    void countsCreaturesTappedAfterTriggering() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new DragonscaleGeneral());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        general.tap();
        harness.passBothPriorities();

        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count a creature untapped before resolution")
    void usesTappedCountAtResolution() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new DragonscaleGeneral());
        general.tap();

        advanceToEndStep(player1);
        general.untap();
        harness.passBothPriorities();

        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chooses by current toughness and can bolster an untapped General")
    void choosesUsingCurrentToughness() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new DragonscaleGeneral());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.tap();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
