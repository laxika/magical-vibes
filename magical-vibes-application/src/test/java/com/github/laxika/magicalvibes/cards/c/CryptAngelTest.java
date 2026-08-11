package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

class CryptAngelTest extends BaseCardTest {

    private void castCryptAngel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CryptAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a blue creature card from the graveyard to hand")
    void etbReturnsBlueCreatureToHand() {
        AirElemental airElemental = new AirElemental();
        harness.setGraveyard(player1, List.of(airElemental));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(airElemental.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertNotInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("ETB returns a red creature card from the graveyard to hand")
    void etbReturnsRedCreatureToHand() {
        HillGiant hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(hillGiant));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(hillGiant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("A green creature card is not a legal target")
    void greenCreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
