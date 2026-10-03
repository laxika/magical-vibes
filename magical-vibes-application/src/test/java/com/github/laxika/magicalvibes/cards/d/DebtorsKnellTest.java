package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.h.HatchingPlans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DebtorsKnell.class, Gristleback.class, HatchingPlans.class})
class DebtorsKnellTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from any graveyard under its controller's control")
    void returnsCreatureFromAnyGraveyardUnderItsControllerControl() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card creature = new Gristleback();
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gristleback");
        harness.assertNotOnBattlefield(player2, "Gristleback");
        harness.assertNotInGraveyard(player2, "Gristleback");
    }

    @Test
    @DisplayName("Returns a target creature card from its controller's graveyard")
    void returnsCreatureFromItsControllersGraveyard() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card gristleback = new Gristleback();
        harness.setGraveyard(player1, List.of(gristleback));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(gristleback.getId());

        harness.handleMultipleCardsChosen(player1, List.of(gristleback.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gristleback");
        harness.assertNotInGraveyard(player1, "Gristleback");
    }

    @Test
    @DisplayName("Only creature cards are legal upkeep targets")
    void onlyCreatureCardsAreLegalTargets() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        harness.setGraveyard(player2, List.of(new HatchingPlans()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Hatching Plans");
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        harness.setGraveyard(player2, List.of(new Gristleback()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Gristleback");
        harness.assertNotOnBattlefield(player1, "Gristleback");
    }

    @Test
    @DisplayName("With empty graveyards no ability remains on the stack")
    void noLegalTargetWithEmptyGraveyards() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Offers creature targets from both graveyards but excludes noncreatures")
    void offersCreatureTargetsFromBothGraveyards() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card ownCreature = new Gristleback();
        Card opposingCreature = new Gristleback();
        harness.setGraveyard(player1, List.of(ownCreature, new HatchingPlans()));
        harness.setGraveyard(player2, List.of(opposingCreature, new HatchingPlans()));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gristleback");
        harness.assertNotInGraveyard(player2, "Gristleback");
        assertThat(countPermanents(player1, "Gristleback")).isEqualTo(1);
        assertThat(findPermanent(player1, "Gristleback").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Gristleback").isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Does not choose a replacement when its target leaves the graveyard")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card target = new Gristleback();
        Card otherCreature = new Gristleback();
        harness.setGraveyard(player2, List.of(target, otherCreature));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gristleback");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A triggered ability still resolves after Debtors' Knell leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card creature = new Gristleback();
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gristleback");
        harness.assertNotInGraveyard(player2, "Gristleback");
    }
}
