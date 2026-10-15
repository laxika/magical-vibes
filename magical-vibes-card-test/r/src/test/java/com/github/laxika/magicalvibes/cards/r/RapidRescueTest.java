package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RapidRescue.class, Forest.class})
class RapidRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards, may return a milled permanent, and gains 2 life")
    void millsReturnsPermanentAndGainsLife() {
        harness.setLife(player1, 20);
        setTopCards(new Forest(), new RapidRescue());

        castAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains 2 life when the optional return is declined")
    void decliningReturnStillGainsLife() {
        harness.setLife(player1, 20);
        setTopCards(new Forest(), new RapidRescue());

        castAndResolve();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains 2 life without offering a nonpermanent card")
    void noPermanentMilled() {
        harness.setLife(player1, 20);
        setTopCards(new RapidRescue(), new RapidRescue());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void returnsOnlyOneOfTwoMilledPermanents() {
        Forest first = new Forest();
        Forest second = new Forest();
        setTopCards(first, second);
        harness.setLife(player1, 20);

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    @Test
    void canDeclineFirstPermanentAndReturnSecond() {
        Forest first = new Forest();
        Forest second = new Forest();
        setTopCards(first, second);
        harness.setLife(player1, 20);

        castAndResolve();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    @Test
    void cannotReturnPermanentAlreadyInGraveyard() {
        Forest oldCard = new Forest();
        harness.setGraveyard(player1, List.of(oldCard));
        setTopCards(new RapidRescue(), new RapidRescue());
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldCard);
        harness.assertLife(player1, 22);
    }

    @Test
    void millsAvailableCardWhenLibraryHasOnlyOneCard() {
        Forest onlyCard = new Forest();
        setTopCards(onlyCard);
        harness.setLife(player1, 20);

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        harness.assertLife(player1, 22);
    }

    @Test
    void gainsLifeWithEmptyLibrary() {
        setTopCards();
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 22);
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RapidRescue(), "{G}");
        harness.passBothPriorities();
    }

    private void setTopCards(com.github.laxika.magicalvibes.model.Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
