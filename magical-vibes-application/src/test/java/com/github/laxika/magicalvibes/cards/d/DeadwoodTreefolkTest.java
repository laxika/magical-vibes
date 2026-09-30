package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.ReveredDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DeadwoodTreefolk.class, ReveredDead.class, Damnation.class})
class DeadwoodTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters and returns another creature card to hand")
    void entersWithCountersAndReturnsCreature() {
        Card creature = new ReveredDead();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        castDeadwoodTreefolk();

        Permanent treefolk = findPermanent(player1, "Deadwood Treefolk");
        assertThat(treefolk.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Revered Dead");
        harness.assertNotInGraveyard(player1, "Revered Dead");
    }

    @Test
    @DisplayName("Returns another creature card when it leaves the battlefield")
    void leavesBattlefieldReturnsCreature() {
        DeadwoodTreefolk treefolkCard = new DeadwoodTreefolk();
        harness.addToBattlefield(player1, treefolkCard);
        Card creature = new ReveredDead();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));

        destroyWithDamnation();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(creature.getId());
        assertThat(choice.validCardIds()).doesNotContain(treefolkCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Revered Dead");
        harness.assertInGraveyard(player1, "Deadwood Treefolk");
    }

    @Test
    @DisplayName("Removes one time counter at upkeep while counters remain")
    void removesTimeCounterAtUpkeepBeforeLast() {
        Permanent treefolk = harness.enterBattlefieldAndReturn(player1, new DeadwoodTreefolk());
        assertThat(treefolk.getCounterCount(CounterType.TIME)).isEqualTo(3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(treefolk.getCounterCount(CounterType.TIME)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Deadwood Treefolk");
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

        destroyWithDamnation();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Deadwood Treefolk");
    }

    @Test
    @DisplayName("Ignores noncreature cards in the graveyard")
    void noncreatureCardCannotBeReturned() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Damnation())));

        harness.enterBattlefieldAndReturn(player1, new DeadwoodTreefolk());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Damnation");
    }

    private void castDeadwoodTreefolk() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DeadwoodTreefolk(), "{5}{G}");
        harness.passBothPriorities();
    }

    private void destroyWithDamnation() {
        harness.castFromHand(player1, new Damnation(), "{2}{B}{B}");
        harness.passBothPriorities();
    }
}
