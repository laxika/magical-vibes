package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TextbookTabulator.class, GrizzlyBears.class})
class TextbookTabulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 2")
    void entersWithSurveilTwo() {
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top0, top1));

        harness.setHand(player1, List.of(new TextbookTabulator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);
    }

    @Test
    @DisplayName("Casting a two-mana spell triggers Increment")
    void castingTwoManaSpellAddsCounter() {
        Permanent tabulator = harness.addToBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(tabulator.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void surveilCanPutOneCardIntoGraveyardAndKeepTheOther() {
        Card first = new TextbookTabulator();
        Card second = new TextbookTabulator();
        Card third = new TextbookTabulator();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.enterBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrder() {
        Card first = new TextbookTabulator();
        Card second = new TextbookTabulator();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsIntoGraveyard() {
        Card first = new TextbookTabulator();
        Card second = new TextbookTabulator();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void surveilWithOnlyOneCardCanPutItIntoGraveyard() {
        Card only = new TextbookTabulator();
        harness.setLibrary(player1, List.of(only));
        harness.enterBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(only);
    }

    @Test
    void surveilWithEmptyLibraryCompletesWithoutLosing() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertOnBattlefield(player1, "Textbook Tabulator");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void incrementDoesNotTriggerWhenManaSpentEqualsPower() {
        Permanent tabulator = harness.addToBattlefieldAndReturn(player1, new TextbookTabulator());
        tabulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(tabulator.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void incrementRechecksPowerWhenItsAbilityResolves() {
        Permanent tabulator = harness.addToBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);

        tabulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(tabulator.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void opponentsSpellDoesNotTriggerIncrement() {
        Permanent tabulator = harness.addToBattlefieldAndReturn(player1, new TextbookTabulator());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(tabulator.getPlusOnePlusOneCounters()).isZero();
    }
}
