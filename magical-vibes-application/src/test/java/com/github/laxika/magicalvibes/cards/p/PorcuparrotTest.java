package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Porcuparrot.class)
class PorcuparrotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals no damage before Porcuparrot mutates")
    void dealsNoDamageBeforeMutating() {
        addCreatureReady(player1, new Porcuparrot());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage equal to the number of times Porcuparrot mutated")
    void damageScalesWithMutations() {
        Permanent porcuparrot = addCreatureReady(player1, new Porcuparrot());
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, porcuparrot, List.of(porcuparrot.getCard()), player1.getId()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
