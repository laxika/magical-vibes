package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReinforcedRonin.class})
class ReinforcedRoninTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand at its controller's end step")
    void returnsSelfAtControllerEndStep() {
        harness.addToBattlefield(player1, new ReinforcedRonin());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reinforced Ronin");
        harness.assertInHand(player1, "Reinforced Ronin");
    }

    @Test
    @DisplayName("Channeling Reinforced Ronin draws a card")
    void channelsAndDrawsCard() {
        harness.setHand(player1, List.of(new ReinforcedRonin()));
        ReinforcedRonin drawnCard = new ReinforcedRonin();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Reinforced Ronin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reinforced Ronin");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotReturnAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new ReinforcedRonin());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reinforced Ronin");
        harness.assertNotInHand(player1, "Reinforced Ronin");
    }

    @Test
    void returnsStolenRoninToOwnerAtControllersEndStep() {
        Permanent ronin = harness.addToBattlefieldAndReturn(player2, new ReinforcedRonin());
        gd.stolenCreatures.put(ronin.getId(), player1.getId());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Reinforced Ronin");
        harness.assertInHand(player1, "Reinforced Ronin");
        harness.assertNotInHand(player2, "Reinforced Ronin");
    }

    @Test
    void enteringAfterEndStepBeginsDoesNotTriggerReturn() {
        advanceToEndStep(player1);
        harness.addToBattlefield(player1, new ReinforcedRonin());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reinforced Ronin");
        harness.assertNotInHand(player1, "Reinforced Ronin");
    }

    @Test
    void canChannelDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new ReinforcedRonin()));
        ReinforcedRonin drawnCard = new ReinforcedRonin();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reinforced Ronin");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void cannotChannelWithoutRedMana() {
        ReinforcedRonin ronin = new ReinforcedRonin();
        harness.setHand(player1, List.of(ronin));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ronin);
        harness.assertNotInGraveyard(player1, "Reinforced Ronin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChannelWithoutGenericMana() {
        ReinforcedRonin ronin = new ReinforcedRonin();
        harness.setHand(player1, List.of(ronin));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ronin);
        harness.assertNotInGraveyard(player1, "Reinforced Ronin");
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
