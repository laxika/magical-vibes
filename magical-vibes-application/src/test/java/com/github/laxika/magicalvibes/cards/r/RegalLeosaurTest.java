package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalLeosaur.class, GrizzlyBears.class})
class RegalLeosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating Regal Leosaur gives other creatures you control +2/+1 until end of turn")
    void mutationBoostsOtherOwnCreaturesUntilEndOfTurn() {
        Permanent leosaur = addCreatureReady(player1, new RegalLeosaur());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        mutate(leosaur);

        assertThat(gqs.getEffectivePower(gd, leosaur)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leosaur)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    private void mutate(Permanent creature) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, creature, List.of(creature.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
