package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowWitches.class, YouthfulKnight.class, GrizzlyBears.class})
class BarrowWitchesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target Knight card from the controller's graveyard to hand")
    void returnsTargetKnightFromGraveyardToHand() {
        YouthfulKnight knight = new YouthfulKnight();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(knight, bears));

        castBarrowWitches();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(knight.getId());

        harness.handleMultipleCardsChosen(player1, List.of(knight.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Youthful Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not target a non-Knight card")
    void doesNotTargetNonKnight() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castBarrowWitches();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castBarrowWitches() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarrowWitches()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
