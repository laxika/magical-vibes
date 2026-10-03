package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrionLocust.class, Disfigure.class})
class CarrionLocustTest extends BaseCardTest {

    private void castCarrionLocust(Card graveyardCard) {
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionLocust(), "{2}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("exiling a creature card makes its graveyard's owner lose 1 life")
    void creatureCardCausesLifeLoss() {
        Card creature = new CarrionLocust();

        castCarrionLocust(creature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("exiling a noncreature card does not cause life loss")
    void noncreatureCardDoesNotCauseLifeLoss() {
        Card instant = new Disfigure();

        castCarrionLocust(instant);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(instant.getId()));
    }

    @Test
    @DisplayName("only opponent graveyards are legal targets")
    void ownGraveyardIsNotTargetable() {
        Card instant = new Disfigure();
        harness.setGraveyard(player1, List.of(instant));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionLocust(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("an empty opponent graveyard leaves the creature on the battlefield without life loss")
    void emptyOpponentGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionLocust(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Carrion Locust");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("only one opponent card is exiled and the controller does not gain life")
    void exilesExactlyOneOpponentCard() {
        Card ownCard = new CarrionLocust();
        Card target = new CarrionLocust();
        Card otherCard = new Disfigure();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(target, otherCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionLocust(), "{2}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactlyInAnyOrder(target, otherCard);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("a target that leaves the graveyard before resolution causes no life loss")
    void targetLeavesGraveyardBeforeResolution() {
        Card target = new CarrionLocust();
        castCarrionLocust(target);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("the trigger still exiles and causes life loss after Carrion Locust dies")
    void sourceLeavesBattlefieldBeforeResolution() {
        Card target = new CarrionLocust();
        castCarrionLocust(target);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Carrion Locust"));

        harness.assertNotOnBattlefield(player1, "Carrion Locust");
        harness.assertInGraveyard(player1, "Carrion Locust");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
