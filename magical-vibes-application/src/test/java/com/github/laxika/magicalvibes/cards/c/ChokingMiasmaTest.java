package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChokingMiasma.class, HillGiant.class})
class ChokingMiasmaTest extends BaseCardTest {

    @Test
    void withoutKickerGivesAllCreaturesMinusTwoMinusTwo() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(false);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void kickedSpellChoosesAControlledCreatureForTheCounter() {
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent otherControlledCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareSpell(true);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosenCreature.getId(), otherControlledCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenCreature.getId()));
        harness.passBothPriorities();

        assertThat(chosenCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(chosenCreature.getEffectivePower()).isEqualTo(2);
        assertThat(otherControlledCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
    }

    @Test
    void creatureDebuffWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(false);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    private void cast(boolean kicked) {
        prepareSpell(kicked);
        if (kicked) {
            harness.castKickedSorcery(player1, 0);
        } else {
            harness.castSorcery(player1, 0);
        }
        harness.passBothPriorities();
    }

    private void prepareSpell(boolean kicked) {
        harness.setHand(player1, List.of(new ChokingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        if (kicked) {
            harness.addMana(player1, ManaColor.GREEN, 1);
        }
    }
}
