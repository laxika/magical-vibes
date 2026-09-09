package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaviorOfTheSmall.class, GrizzlyBears.class, SibilantSpirit.class, Forest.class})
class SaviorOfTheSmallTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Savior returns a target creature with mana value 3 or less to hand")
    void tappedSaviorReturnsCheapCreatureToHand() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only qualifying creatures from its controller's graveyard are legal targets")
    void onlyQualifyingCreaturesAreLegalTargets() {
        Permanent savior = addSavior();
        savior.tap();
        Card cheapCreature = new GrizzlyBears();
        Card expensiveCreature = new SibilantSpirit();
        Card noncreature = new Forest();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(cheapCreature, expensiveCreature, noncreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(cheapCreature.getId());
    }

    @Test
    @DisplayName("The survival ability does not return a card if Savior becomes untapped before resolution")
    void untappingBeforeResolutionPreventsReturn() {
        Permanent savior = addSavior();
        savior.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);
        savior.untap();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The survival ability does not trigger while Savior is untapped")
    void untappedSaviorDoesNotTrigger() {
        addSavior();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private Permanent addSavior() {
        return harness.addToBattlefieldAndReturn(player1, new SaviorOfTheSmall());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
