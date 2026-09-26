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

@CardUsed({GiantWarthog.class, HuntingGrounds.class, MentalNote.class})
class HuntingGroundsTest extends BaseCardTest {

    @Test
    void thresholdLetsControllerPutCreatureFromHandOntoBattlefieldWhenOpponentCastsSpell() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new HuntingGrounds());
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentSpell(new GiantWarthog(), "{5}{G}");

        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        PendingInteraction.HandCardChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(handChoice).isNotNull();
        assertThat(handChoice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void thresholdAbilityIsInactiveBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new HuntingGrounds());
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentSpell(new GiantWarthog(), "{5}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningMayChoiceLeavesCreatureInHand() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new HuntingGrounds());
        Card creature = new GiantWarthog();
        harness.setHand(player1, List.of(creature));
        castOpponentSpell(new GiantWarthog(), "{5}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void thresholdAbilityTriggersForNoncreatureOpponentSpell() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new HuntingGrounds());
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentSpell(new MentalNote(), "{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Giant Warthog");
    }

    @Test
    void mayChoiceOffersOnlyCreatureCardsFromHand() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new HuntingGrounds());
        Card noncreature = new MentalNote();
        Card creature = new GiantWarthog();
        harness.setHand(player1, List.of(noncreature, creature));
        castOpponentSpell(new GiantWarthog(), "{5}{G}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        PendingInteraction.HandCardChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(handChoice).isNotNull();
        assertThat(handChoice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(noncreature);
    }

    private void castOpponentSpell(Card spell, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, spell, manaCost);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GiantWarthog())
                .toList();
    }

    @Test
    void onlyCreatureCardsAreEligibleFromHand() {
        addHuntingGroundsForJudReview(7);
        harness.setHand(player1, List.of(new MentalNote()));
        castOpponentCreatureSpellForJudReview();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mental Note");
        harness.assertInHand(player1, "Mental Note");
    }

    @Test
    void controllerCastingSpellDoesNotTriggerAbility() {
        addHuntingGroundsForJudReview(7);
        harness.castFromHand(player1, new GiantWarthog(), "{5}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void triggeredAbilityStillResolvesAfterThresholdIsLost() {
        addHuntingGroundsForJudReview(7);
        harness.setHand(player1, List.of(new GiantWarthog()));
        castOpponentCreatureSpellForJudReview();

        harness.handleMayAbilityChosen(player1, true);
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Giant Warthog");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addHuntingGroundsForJudReview(int graveyardSize) {
        harness.setGraveyard(player1, graveyardWithCards(graveyardSize));
        harness.addToBattlefield(player1, new HuntingGrounds());
    }

    private void castOpponentCreatureSpellForJudReview() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GiantWarthog(), "{5}{G}");
    }
}
