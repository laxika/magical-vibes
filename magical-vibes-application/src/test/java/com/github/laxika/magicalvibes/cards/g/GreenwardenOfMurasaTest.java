package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenwardenOfMurasa.class, LightningBolt.class, WrathOfGod.class})
class GreenwardenOfMurasaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a chosen card from its controller's graveyard to hand")
    void enterTriggerReturnsChosenCardFromOwnGraveyard() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));

        castGreenwarden();

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
    @DisplayName("Declining the ETB return leaves the card in the graveyard")
    void decliningEnterTriggerReturnsNothing() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));

        castGreenwarden();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertNotInHand(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Death trigger may exile Greenwarden and return a chosen card to hand")
    void deathTriggerExilesSourceAndReturnsChosenCard() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(card.getId(), greenwarden.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiled -> exiled.getId().equals(greenwarden.getId()));
        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Declining the death trigger leaves Greenwarden in the graveyard")
    void decliningDeathTriggerLeavesSourceInGraveyard() {
        Card card = new LightningBolt();
        harness.setGraveyard(player1, List.of(card));
        Card greenwarden = harness.addToBattlefieldAndReturn(player1, new GreenwardenOfMurasa()).getCard();

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(graveyardCard -> graveyardCard.getId().equals(greenwarden.getId()));
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    private void castGreenwarden() {
        harness.setHand(player1, List.of(new GreenwardenOfMurasa()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
