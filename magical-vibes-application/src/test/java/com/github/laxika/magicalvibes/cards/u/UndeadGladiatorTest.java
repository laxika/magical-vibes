package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndeadGladiator.class, GrizzlyBears.class})
class UndeadGladiatorTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardToHandAfterDiscardingDuringUpkeep() {
        UndeadGladiator gladiator = new UndeadGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Undead Gladiator");
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    void cyclesFromHandByDiscardingItAndDrawing() {
        harness.setHand(player1, List.of(new UndeadGladiator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new GrizzlyBears())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Undead Gladiator");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
