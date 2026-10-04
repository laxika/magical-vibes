package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Graveshifter.class, Giantfall.class})
class GraveshifterTest extends BaseCardTest {

    private void castGraveshifter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Graveshifter(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted creature card from the graveyard to hand")
    void etbReturnsCreatureToHand() {
        Graveshifter creature = new Graveshifter();
        harness.setGraveyard(player1, List.of(creature));

        castGraveshifter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Graveshifter");
        harness.assertNotInGraveyard(player1, "Graveshifter");
    }

    @Test
    @DisplayName("ETB return can be declined")
    void returnCanBeDeclined() {
        Graveshifter creature = new Graveshifter();
        harness.setGraveyard(player1, List.of(creature));

        castGraveshifter();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Graveshifter");
        harness.assertNotInHand(player1, "Graveshifter");
    }

    @Test
    @DisplayName("Noncreature cards are not legal graveyard targets")
    void nonCreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new Giantfall()));

        castGraveshifter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Giantfall");
    }

    @Test
    @DisplayName("A legal target is required even when the controller intends to decline the return")
    void targetIsRequiredBeforeResolution() {
        harness.setGraveyard(player1, List.of(new Graveshifter()));

        castGraveshifter();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature cards in an opponent's graveyard cannot be targeted")
    void opponentsGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new Graveshifter()));

        castGraveshifter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Graveshifter");
        harness.assertNotInHand(player1, "Graveshifter");
    }

    @Test
    @DisplayName("Empty graveyard produces no graveyard choice")
    void emptyGraveyardProducesNoChoice() {
        castGraveshifter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Graveshifter");
    }
}
