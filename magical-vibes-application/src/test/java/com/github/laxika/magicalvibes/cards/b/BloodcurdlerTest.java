package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bloodcurdler.class, AngelicWall.class})
class BloodcurdlerTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a card at the beginning of its controller's upkeep")
    void millsAtUpkeep() {
        harness.setGraveyard(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new AngelicWall());
        harness.addToBattlefield(player1, new Bloodcurdler());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gets +1/+1 with seven cards in its controller's graveyard")
    void thresholdBoostsPowerAndToughness() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new Bloodcurdler());

        Permanent bloodcurdler = findBloodcurdler();

        assertThat(gqs.getEffectivePower(gd, bloodcurdler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloodcurdler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles two cards from its graveyard at its controller's end step")
    void exilesTwoCardsAtEndStep() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.setGraveyard(player2, List.of(new AngelicWall()));
        harness.addToBattlefield(player1, new Bloodcurdler());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        Permanent bloodcurdler = findBloodcurdler();
        assertThat(gqs.getEffectivePower(gd, bloodcurdler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bloodcurdler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores an opponent's graveyard for threshold")
    void thresholdIgnoresOpponentsGraveyard() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.setGraveyard(player2, List.of());
        harness.addToBattlefield(player2, new Bloodcurdler());

        Permanent bloodcurdler = findPermanent(player2, "Bloodcurdler");

        assertThat(gqs.getEffectivePower(gd, bloodcurdler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bloodcurdler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void triggersOnlyDuringControllersEndStep() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new Bloodcurdler());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not gain threshold abilities with fewer than seven cards")
    void noThresholdBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.addToBattlefield(player1, new Bloodcurdler());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        Permanent bloodcurdler = findBloodcurdler();
        assertThat(gqs.getEffectivePower(gd, bloodcurdler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bloodcurdler)).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private Permanent findBloodcurdler() {
        return findPermanent(player1, "Bloodcurdler");
    }

    private List<Card> graveyardWithSevenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new AngelicWall());
        }
        return cards;
    }
}
