package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HakbalOfTheSurgingSoul.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class, Forest.class})
class HakbalOfTheSurgingSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat makes each Merfolk you control explore")
    void beginningOfCombatMakesEachMerfolkExplore() {
        Permanent hakbal = addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        addCreatureReady(player1, new GrizzlyBears());
        Card landBeforeNonland = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(landBeforeNonland, nonland));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(landBeforeNonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        assertThat(hakbal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking may put a land from hand onto the battlefield")
    void attackingPutsLandFromHandOntoBattlefield() {
        addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Card land = new Forest();
        harness.setHand(player1, List.of(land));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && !permanent.isTapped());
    }

    @Test
    @DisplayName("Declining the attack trigger draws a card")
    void decliningAttackTriggerDrawsCard() {
        addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Controller chooses which Merfolk explores before revealing a card")
    void controllerChoosesExploreOrder() {
        Permanent hakbal = addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(land, nonland));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(hakbal.getId(), merfolk.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, nonland);
        harness.handlePermanentChosen(player1, merfolk.getId());
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, hakbal.getId());
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(hakbal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A revealed nonland can stay on top after exploring")
    void keepsRevealedNonlandOnTop() {
        Permanent hakbal = addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Card nonland = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(nonland));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hakbal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exploring an empty library still puts a counter on the creature")
    void emptyLibraryStillGivesCounter() {
        Permanent hakbal = addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Permanent opponentMerfolk = addCreatureReady(player2, new MerfolkOfThePearlTrident());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        advanceToCombat(player1);

        assertThat(hakbal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentMerfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Hakbal does not make creatures explore during an opponent's combat")
    void doesNotExploreDuringOpponentsCombat() {
        Permanent hakbal = addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));

        advanceToCombat(player2);

        assertThat(hakbal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting the attack choice without a land draws instead")
    void acceptingWithoutLandDrawsInstead() {
        addCreatureReady(player1, new HakbalOfTheSurgingSoul());
        Card nonlandInHand = new GrizzlyBears();
        Card topCard = new Forest();
        harness.setHand(player1, List.of(nonlandInHand));
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonlandInHand, topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == topCard);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
