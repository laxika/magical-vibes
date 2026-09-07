package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzzetChronarch.class, Divination.class, GrizzlyBears.class, Shock.class})
class IzzetChronarchTest extends BaseCardTest {

    private void castIzzetChronarch() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new IzzetChronarch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted instant card from graveyard to hand")
    void etbReturnsInstantToHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB returns a targeted sorcery card from graveyard to hand")
    void etbReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castIzzetChronarch();

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("A creature card in the graveyard is not a legal target")
    void creatureNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castIzzetChronarch();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
