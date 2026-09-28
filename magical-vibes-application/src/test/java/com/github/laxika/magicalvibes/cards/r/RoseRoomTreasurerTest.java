package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoseRoomTreasurer.class, GrizzlyBears.class})
class RoseRoomTreasurerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates Treasures on the first two resolutions, then pays X for reflexive damage")
    void createsTwoTreasuresThenDealsPaidDamage() {
        addCreatureReady(player1, new RoseRoomTreasurer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 8);

        castCreatureAndResolveTrigger();
        castCreatureAndResolveTrigger();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);

        castCreatureAndResolveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger for Rose Room Treasurer entering under its controller's control")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new RoseRoomTreasurer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castCreatureAndResolveTrigger() {
        castCreatureUntilTriggerResolves();
        harness.passBothPriorities();
    }

    private void castCreatureUntilTriggerResolves() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
