package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EternalWitnessTest extends BaseCardTest {

    private void castEternalWitness() {
        harness.setHand(player1, List.of(new EternalWitness()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a chosen card from its controller's graveyard to hand")
    void returnsChosenCardFromOwnGraveyard() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("ETB can return any card type")
    void returnsCreatureCard() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional return leaves the card in the graveyard")
    void decliningReturnsNothing() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));

        castEternalWitness();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertNotInHand(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("ETB does not target cards in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        Card card = new LightningBolt();
        harness.setGraveyard(player2, List.of(card));

        castEternalWitness();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }
}
