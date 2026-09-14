package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadlyDesigns.class, GrizzlyBears.class})
class DeadlyDesignsTest extends BaseCardTest {

    @Test
    void anyPlayerMayAddPlotCounters() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(designs.getCounterCount(CounterType.PLOT)).isEqualTo(1);
    }

    @Test
    void fivePlotCountersSacrificeAndDestroyUpToTwoCreatures() {
        Permanent designs = harness.addToBattlefieldAndReturn(player1, new DeadlyDesigns());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        designs.setCounterCount(CounterType.PLOT, 4);

        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();

        designs.setCounterCount(CounterType.PLOT, 5);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deadly Designs");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deadly Designs");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstCreature.getCard(), secondCreature.getCard());
    }
}
