package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.Overmaster;
import com.github.laxika.magicalvibes.cards.p.PardicLancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnOfTheDead.class, Overmaster.class, PardicLancer.class})
class DawnOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("You lose 1 life during your upkeep")
    void losesLifeDuringUpkeep() {
        harness.addToBattlefield(player1, new DawnOfTheDead());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The upkeep trigger returns a targeted creature with haste")
    void returnsTargetedCreatureWithHaste() {
        Card creature = new PardicLancer();
        Card spell = new Overmaster();
        harness.addToBattlefield(player1, new DawnOfTheDead());
        harness.setGraveyard(player1, List.of(creature, spell));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Pardic Lancer");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(returned.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));
    }

    @Test
    @DisplayName("The upkeep trigger targets only a creature card in your graveyard")
    void targetsOnlyMatchingCreatureInYourGraveyard() {
        Card ownCreature = new PardicLancer();
        Card nonCreature = new Overmaster();
        Card opponentCreature = new PardicLancer();
        harness.addToBattlefield(player1, new DawnOfTheDead());
        harness.setGraveyard(player1, List.of(ownCreature, nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pardic Lancer");
        harness.assertInGraveyard(player1, "Overmaster");
        harness.assertInGraveyard(player2, "Pardic Lancer");
    }

    @Test
    @DisplayName("The returned creature is exiled at the next end step")
    void returnedCreatureIsExiledAtNextEndStep() {
        Card creature = new PardicLancer();
        harness.addToBattlefield(player1, new DawnOfTheDead());
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).as("delayed actions after end step")
                .isEmpty();

        harness.assertNotOnBattlefield(player1, "Pardic Lancer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The optional upkeep trigger can be declined")
    void canDeclineReturn() {
        Card creature = new PardicLancer();
        harness.addToBattlefield(player1, new DawnOfTheDead());
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pardic Lancer");
        harness.assertNotOnBattlefield(player1, "Pardic Lancer");
    }
}
