package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalvagerOfSecretsTest extends BaseCardTest {

    /** Casts Salvager of Secrets and resolves it so its ETB trigger sets up graveyard targeting. */
    private void castSalvager() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SalvagerOfSecrets()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted instant card from the graveyard to hand")
    void etbReturnsInstantToHand() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));

        castSalvager();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("ETB can return a sorcery card from the graveyard to hand")
    void etbReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castSalvager();

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Divination"));
    }

    @Test
    @DisplayName("A creature card is not a legal target")
    void creatureCardNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castSalvager();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
