package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndeadGladiator.class, Swamp.class})
class UndeadGladiatorTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardToHandAfterDiscardingDuringUpkeep() {
        UndeadGladiator gladiator = new UndeadGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        harness.setHand(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Undead Gladiator");
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    void cannotReturnFromGraveyardWithoutACardToDiscard() {
        harness.setGraveyard(player1, List.of(new UndeadGladiator()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canOnlyActivateGraveyardAbilityDuringYourUpkeep() {
        harness.setGraveyard(player1, List.of(new UndeadGladiator()));
        harness.setHand(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    void cannotReturnFromGraveyardDuringOpponentUpkeep() {
        harness.setGraveyard(player1, List.of(new UndeadGladiator()));
        harness.setHand(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    @Test
    void returnsOnlyTheActivatedCopyFromGraveyard() {
        UndeadGladiator otherGladiator = new UndeadGladiator();
        UndeadGladiator activatedGladiator = new UndeadGladiator();
        Swamp discardedCard = new Swamp();
        harness.setGraveyard(player1, List.of(otherGladiator, activatedGladiator));
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 1);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(activatedGladiator);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(otherGladiator, discardedCard);
    }

    @Test
    void cyclesFromHandByDiscardingItAndDrawing() {
        harness.setHand(player1, List.of(new UndeadGladiator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Undead Gladiator");
        harness.assertInHand(player1, "Swamp");
    }

    @Test
    void paysDiscardCostBeforeReturnResolves() {
        UndeadGladiator gladiator = new UndeadGladiator();
        Swamp discardedCard = new Swamp();
        harness.setGraveyard(player1, List.of(gladiator));
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(gladiator, discardedCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(gladiator);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
    }

    @Test
    void canCycleReturnAndCycleAgainDuringSameUpkeep() {
        UndeadGladiator gladiator = new UndeadGladiator();
        Swamp firstDraw = new Swamp();
        Swamp secondDraw = new Swamp();
        harness.setHand(player1, List.of(gladiator));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gladiator);

        harness.passBothPriorities();
        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(gladiator, firstDraw);
    }

    @Test
    void earlierReturnAbilityDoesNotReturnCardThatLeftAndReenteredGraveyard() {
        UndeadGladiator gladiator = new UndeadGladiator();
        Swamp firstDiscard = new Swamp();
        Swamp secondDiscard = new Swamp();
        Swamp drawnCard = new Swamp();
        harness.setGraveyard(player1, List.of(gladiator));
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(gladiator);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(gladiator, firstDiscard, secondDiscard);
    }
}
