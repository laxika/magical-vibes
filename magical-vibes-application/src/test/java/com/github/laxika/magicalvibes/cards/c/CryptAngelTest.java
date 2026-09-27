package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LlanowarCavalry;
import com.github.laxika.magicalvibes.cards.s.Skizzik;
import com.github.laxika.magicalvibes.cards.t.TolarianEmissary;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptAngel.class, TolarianEmissary.class, Skizzik.class, LlanowarCavalry.class, Cremate.class})
class CryptAngelTest extends BaseCardTest {

    private void castCryptAngel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CryptAngel(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a blue creature card from the graveyard to hand")
    void etbReturnsBlueCreatureToHand() {
        TolarianEmissary blueCreature = new TolarianEmissary();
        harness.setGraveyard(player1, List.of(blueCreature));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(blueCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tolarian Emissary");
        harness.assertNotInGraveyard(player1, "Tolarian Emissary");
    }

    @Test
    @DisplayName("ETB returns a red creature card from the graveyard to hand")
    void etbReturnsRedCreatureToHand() {
        Skizzik redCreature = new Skizzik();
        harness.setGraveyard(player1, List.of(redCreature));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(redCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skizzik");
        harness.assertNotInGraveyard(player1, "Skizzik");
    }

    @Test
    @DisplayName("A green creature card is not a legal target")
    void greenCreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new LlanowarCavalry()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Llanowar Cavalry");
    }

    @Test
    @DisplayName("A creature card in an opponent's graveyard is not a legal target")
    void opponentCreatureIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new TolarianEmissary()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Tolarian Emissary");
    }

    @Test
    @DisplayName("A noncreature card is not a legal target")
    void noncreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new Cremate()));

        castCryptAngel();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Cremate");
    }
}
