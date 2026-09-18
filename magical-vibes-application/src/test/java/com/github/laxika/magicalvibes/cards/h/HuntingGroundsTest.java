package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntingGrounds.class, GiantWarthog.class, MentalNote.class})
class HuntingGroundsTest extends BaseCardTest {

    @Test
    void thresholdLetsControllerPutCreatureFromHandOntoBattlefieldWhenOpponentCastsSpell() {
        addHuntingGrounds(7);
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentCreatureSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void thresholdAbilityIsInactiveBelowSevenCards() {
        addHuntingGrounds(6);
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentCreatureSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningMayChoiceLeavesCreatureInHand() {
        addHuntingGrounds(7);
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentCreatureSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void onlyCreatureCardsAreEligibleFromHand() {
        addHuntingGrounds(7);
        harness.setHand(player1, List.of(new MentalNote()));
        castOpponentCreatureSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mental Note");
        harness.assertInHand(player1, "Mental Note");
    }

    @Test
    void controllerCastingSpellDoesNotTriggerAbility() {
        addHuntingGrounds(7);
        harness.castFromHand(player1, new GiantWarthog(), "{5}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void triggeredAbilityStillResolvesAfterThresholdIsLost() {
        addHuntingGrounds(7);
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentCreatureSpell();

        harness.handleMayAbilityChosen(player1, true);
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addHuntingGrounds(int graveyardSize) {
        harness.setGraveyard(player1, graveyardWithCards(graveyardSize));
        harness.addToBattlefield(player1, new HuntingGrounds());
    }

    private void castOpponentCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GiantWarthog(), "{5}{G}");
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GiantWarthog())
                .toList();
    }
}
