package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.o.OrbsOfWarding;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxsTutelage.class, GrizzlyBears.class, Island.class, LlanowarElves.class,
        Ornithopter.class, Shock.class, OrbsOfWarding.class, RestInPeace.class})
class SphinxsTutelageTest extends BaseCardTest {

    @Test
    @DisplayName("Two milled nonland cards sharing a color repeat the mill")
    void repeatsWhenMilledNonlandsShareAColor() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        // Green + green repeats; the second pair stops on the land.
        harness.setLibrary(player2, List.<Card>of(new GrizzlyBears(), new LlanowarElves(), new Shock(), new Island(),
                new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Milled nonland cards of different colors do not repeat")
    void doesNotRepeatOnDifferentColors() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player2, List.<Card>of(new GrizzlyBears(), new Shock(), new GrizzlyBears(),
                new GrizzlyBears(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Two colorless milled cards share no color, so the mill does not repeat")
    void doesNotRepeatOnColorlessCards() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player2, List.<Card>of(new Ornithopter(), new Ornithopter(), new GrizzlyBears(),
                new LlanowarElves(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An emptying library ends the repeat instead of looping")
    void stopsWhenLibraryRunsOut() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player2, List.<Card>of(new GrizzlyBears(), new LlanowarElves()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's draw does not trigger the mill")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player2, List.<Card>of(new GrizzlyBears(), new LlanowarElves(), new Shock(), new Island()));

        advanceToDraw(player2);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("{5}{U} draws a card, then discards a card")
    void lootAbilityDrawsThenDiscards() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player1, List.<Card>of(new Island(), new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the activated ability

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent with hexproof cannot be milled by the draw trigger")
    void cannotMillOpponentWithHexproof() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.addToBattlefield(player2, new OrbsOfWarding());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock(), new Island()));

        advanceToDraw(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The repeat condition includes milled cards exiled by Rest in Peace")
    void repeatsWhenMilledCardsAreExiled() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.addToBattlefield(player1, new RestInPeace());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new LlanowarElves(),
                new Shock(), new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A single remaining nonland card is milled without repeating")
    void millsSingleRemainingCard() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each successive same-color pair repeats within the same resolution")
    void repeatsForSuccessiveMatchingPairs() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new LlanowarElves(),
                new Shock(), new Shock(), new Island(), new Island(), new Island()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Looting on the opponent's turn triggers milling only after the discard")
    void lootOnOpponentTurnFinishesBeforeMillResolves() {
        harness.addToBattlefield(player1, new SphinxsTutelage());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock(), new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid the first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }
}
