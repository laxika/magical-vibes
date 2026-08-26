package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadwoodTreefolk.class, GrizzlyBears.class, WrathOfGod.class})
class DeadwoodTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters and returns another creature card to hand")
    void entersWithCountersAndReturnsCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        castDeadwoodTreefolk();

        Permanent treefolk = findPermanent(player1, "Deadwood Treefolk");
        assertThat(treefolk.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns another creature card when it leaves the battlefield")
    void leavesBattlefieldReturnsCreature() {
        DeadwoodTreefolk treefolkCard = new DeadwoodTreefolk();
        harness.addToBattlefield(player1, treefolkCard);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        destroyWithWrath();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(bears.getId());
        assertThat(choice.validCardIds()).doesNotContain(treefolkCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deadwood Treefolk");
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new DeadwoodTreefolk());
        treefolk.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Deadwood Treefolk");
        harness.assertInGraveyard(player1, "Deadwood Treefolk");
    }

    @Test
    @DisplayName("Leaves trigger is skipped when no other creature card is in the graveyard")
    void noOtherCreatureCardSkipsLeavesTrigger() {
        harness.addToBattlefield(player1, new DeadwoodTreefolk());

        destroyWithWrath();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Deadwood Treefolk");
    }

    private void castDeadwoodTreefolk() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeadwoodTreefolk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void destroyWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
