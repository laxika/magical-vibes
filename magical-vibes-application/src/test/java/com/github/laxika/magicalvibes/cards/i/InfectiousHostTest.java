package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InfectiousHost.class)
class InfectiousHostTest extends BaseCardTest {

    @Test
    void whenItDiesTargetPlayerLosesTwoLife() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new InfectiousHost());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        host.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
