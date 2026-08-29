package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a target card from a graveyard")
    void etbExilesTargetGraveyardCard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new BloodOperative()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Surveiling may pay 3 life to return Blood Operative from the graveyard")
    void surveilReturnsBloodOperativeToHandForThreeLife() {
        Card bloodOperative = new BloodOperative();
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bloodOperative));
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bloodOperative);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }
}
