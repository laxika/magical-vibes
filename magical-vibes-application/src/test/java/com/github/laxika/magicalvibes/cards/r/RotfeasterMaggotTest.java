package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RotfeasterMaggotTest extends BaseCardTest {

    /** Casts Rotfeaster Maggot and resolves it so the ETB trigger sets up graveyard targeting. */
    private void castMaggot() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RotfeasterMaggot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles a creature card from an opponent's graveyard and gains its toughness in life")
    void exilesOpponentCreatureAndGainsLife() {
        HillGiant giant = new HillGiant(); // 3/3
        harness.setGraveyard(player2, List.of(giant));

        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("The controller's own graveyard is a legal source and the life gained scales with toughness")
    void exilesOwnCreatureAndGainsLife() {
        GrizzlyBears bears = new GrizzlyBears(); // 2/2
        harness.setGraveyard(player1, List.of(bears));

        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("A noncreature card in a graveyard is not a legal target")
    void noncreatureCardNotTargetable() {
        harness.setGraveyard(player2, List.of(new Cancel()));

        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("No creature card in any graveyard means no trigger and no life gain")
    void noCreatureCardNoTrigger() {
        int lifeBefore = gd.getLife(player1.getId());
        castMaggot();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
