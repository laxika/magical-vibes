package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnuridBrushhopper.class, GiantWarthog.class, IronshellBeetle.class})
class AnuridBrushhopperTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding two cards exiles Anurid Brushhopper")
    void discardingTwoCardsExilesIt() {
        addCreatureReady(player1, new AnuridBrushhopper());
        harness.setHand(player1, List.of(new IronshellBeetle(), new IronshellBeetle()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Anurid Brushhopper");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Anurid Brushhopper"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ironshell Beetle", "Ironshell Beetle");
    }

    @Test
    @DisplayName("Anurid Brushhopper returns at the beginning of the next end step")
    void returnsAtNextEndStep() {
        addCreatureReady(player1, new AnuridBrushhopper());
        harness.setHand(player1, List.of(new IronshellBeetle(), new IronshellBeetle()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Anurid Brushhopper");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Anurid Brushhopper"));
    }

    @Test
    @DisplayName("The ability cannot be activated without two cards to discard")
    void requiresTwoCardsToDiscard() {
        addCreatureReady(player1, new AnuridBrushhopper());
        harness.setHand(player1, List.of(new IronshellBeetle()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Anurid Brushhopper");
    }

    @Test
    @DisplayName("Returns under its owner's control when an opponent controls it")
    void returnsUnderOwnersControlWhenControlledByOpponent() {
        AnuridBrushhopper brushhopper = new AnuridBrushhopper();
        brushhopper.setOwnerId(player1.getId());
        Permanent stolenBrushhopper = addCreatureReady(player2, brushhopper);
        gd.stolenCreatures.put(stolenBrushhopper.getId(), player1.getId());
        harness.setHand(player2, List.of(new IronshellBeetle(), new IronshellBeetle()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Anurid Brushhopper");

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Anurid Brushhopper");
        harness.assertNotOnBattlefield(player2, "Anurid Brushhopper");
    }

    @Test
    @DisplayName("Anurid Brushhopper returns under its owner's control")
    void returnsUnderOwnersControl() {
        AnuridBrushhopper brushhopper = new AnuridBrushhopper();
        brushhopper.setOwnerId(player2.getId());
        addCreatureReady(player1, brushhopper);
        harness.setHand(player1, List.of(new GiantWarthog(), new GiantWarthog()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepForJudReview();

        harness.assertNotOnBattlefield(player1, "Anurid Brushhopper");
        harness.assertOnBattlefield(player2, "Anurid Brushhopper");
    }

    private void advanceToEndStepForJudReview() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }
}
