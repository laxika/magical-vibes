package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnTheJob.class, GrizzlyBears.class})
class OnTheJobTest extends BaseCardTest {

    @Test
    void boostsOwnCreaturesAndCreatesAClue() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castOnTheJob();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .allSatisfy(creature -> {
                    assertThat(creature.getEffectivePower()).isEqualTo(4);
                    assertThat(creature.getEffectiveToughness()).isEqualTo(3);
                });
        assertThat(findPermanents(player2, "Grizzly Bears"))
                .allSatisfy(creature -> {
                    assertThat(creature.getEffectivePower()).isEqualTo(2);
                    assertThat(creature.getEffectiveToughness()).isEqualTo(2);
                });
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void creatureBoostEndsAtCleanupButClueRemains() {
        addCreatureReady(player1, new GrizzlyBears());
        castOnTheJob();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private void castOnTheJob() {
        harness.setHand(player1, List.of(new OnTheJob()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
