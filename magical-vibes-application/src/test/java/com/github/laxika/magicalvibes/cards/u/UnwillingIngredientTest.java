package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnwillingIngredientTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles the source, draws a card, and loses 1 life")
    void graveyardAbilityExilesDrawsAndLosesLife() {
        UnwillingIngredient ingredient = new UnwillingIngredient();
        GrizzlyBears cardToDraw = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(ingredient));
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ingredient.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ingredient.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(cardToDraw.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
