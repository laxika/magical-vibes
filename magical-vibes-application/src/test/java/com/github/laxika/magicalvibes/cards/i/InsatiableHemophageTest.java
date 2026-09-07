package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(InsatiableHemophage.class)
class InsatiableHemophageTest extends BaseCardTest {

    @Test
    @DisplayName("Mutations drain life equal to the number of times Insatiable Hemophage mutated")
    void mutationsScaleLifeDrain() {
        Permanent hemophage = addCreatureReady(player1, new InsatiableHemophage());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        mutate(hemophage);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        mutate(hemophage);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private void mutate(Permanent hemophage) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, hemophage, List.of(hemophage.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
