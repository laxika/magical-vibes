package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CliffsideLookout;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlamogsDespoiler.class, CliffsideLookout.class})
class UlamogsDespoilerTest extends BaseCardTest {

    @Test
    void mayPutTwoOpponentOwnedExiledCardsIntoGraveyardsAndEnterWithCounters() {
        CliffsideLookout first = new CliffsideLookout();
        CliffsideLookout second = new CliffsideLookout();
        harness.setExile(player2, List.of(first, second));
        castUlamogsDespoiler();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        Permanent despoiler = findPermanent(player1, "Ulamog's Despoiler");
        assertThat(despoiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, despoiler)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, despoiler)).isEqualTo(9);
    }

    @Test
    void mayDeclineWithoutMovingCardsOrAddingCounters() {
        CliffsideLookout first = new CliffsideLookout();
        CliffsideLookout second = new CliffsideLookout();
        harness.setExile(player2, List.of(first, second));
        castUlamogsDespoiler();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        Permanent despoiler = findPermanent(player1, "Ulamog's Despoiler");
        assertThat(despoiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPromptWithoutTwoEligibleOpponentOwnedCards() {
        CliffsideLookout exiledCard = new CliffsideLookout();
        harness.setExile(player2, List.of(exiledCard));
        castUlamogsDespoiler();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledCard);
        Permanent despoiler = findPermanent(player1, "Ulamog's Despoiler");
        assertThat(despoiler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotProcessOnlyOneCardWhenTwoAreAvailable() {
        UlamogsDespoiler first = new UlamogsDespoiler();
        UlamogsDespoiler second = new UlamogsDespoiler();
        harness.setExile(player2, List.of(first, second));
        castUlamogsDespoiler();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(findPermanent(player1, "Ulamog's Despoiler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void controllerOwnedExiledCardsCannotSupplyTheSecondCard() {
        UlamogsDespoiler ownCard = new UlamogsDespoiler();
        UlamogsDespoiler opponentCard = new UlamogsDespoiler();
        harness.setExile(player1, List.of(ownCard));
        harness.setExile(player2, List.of(opponentCard));
        castUlamogsDespoiler();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(findPermanent(player1, "Ulamog's Despoiler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentOwnedFaceDownCardsAreEligibleForProcessing() {
        UlamogsDespoiler first = new UlamogsDespoiler();
        UlamogsDespoiler second = new UlamogsDespoiler();
        gd.addToExile(player2.getId(), first, null, true);
        gd.addToExile(player2.getId(), second, null, true);
        castUlamogsDespoiler();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("A pile of face-down cards is offered as two picks, which move two different cards of it (2015-08-25 ruling)")
    void faceDownPileIsOfferedAsTwoPicks() {
        List<CliffsideLookout> pile = List.of(new CliffsideLookout(), new CliffsideLookout(), new CliffsideLookout());
        harness.inMutationScope(() -> pile.forEach(card -> gd.addToExile(player2.getId(), card, null, true)));
        castUlamogsDespoiler();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).hasSize(2);
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2).doesNotHaveDuplicates();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Ulamog's Despoiler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void castUlamogsDespoiler() {
        harness.setHand(player1, List.of(new UlamogsDespoiler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
