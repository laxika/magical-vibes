package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TesharAncestorsApostle.class, LlanowarElves.class, ShortSword.class, BalothGorger.class,
        AcademyDrake.class, HistoryOfBenalia.class})
class TesharAncestorsApostleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact triggers graveyard targeting when valid creature in graveyard")
    void artifactTriggerShowsGraveyardChoice() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));
        harness.castFromHand(player1, new ShortSword(), "{1}");

        // Should prompt for graveyard target selection
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Selecting a valid creature returns it to the battlefield")
    void selectingCreatureReturnsItToBattlefield() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));
        harness.castFromHand(player1, new ShortSword(), "{1}");

        // Choose the creature from graveyard
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Teshar, Ancestor's Apostle"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Llanowar Elves should be on the battlefield
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Creature with MV > 3 cannot be targeted")
    void creatureWithHighManaValueNotValid() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        harness.setGraveyard(player1, List.of(new BalothGorger()));
        harness.castFromHand(player1, new ShortSword(), "{1}");

        // No valid graveyard targets → trigger skipped, no graveyard choice prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Non-creature card in graveyard does not qualify as a target")
    void nonCreatureNotValid() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        // Short Sword is an artifact, not a creature
        harness.setGraveyard(player1, List.of(new ShortSword()));
        harness.castFromHand(player1, new ShortSword(), "{1}");

        // No valid graveyard targets → trigger skipped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Empty graveyard leaves no legal target for the trigger")
    void emptyGraveyardNoTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        harness.castFromHand(player1, new ShortSword(), "{1}");

        // No creature cards in graveyard → trigger skipped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Non-historic spell does not trigger")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves graveyardElves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(graveyardElves));

        // Cast a non-historic creature (Llanowar Elves is not legendary/artifact/Saga)
        harness.castFromHand(player1, new LlanowarElves(), "{G}");

        // Only the creature spell on the stack, no graveyard choice prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)).isFalse();
    }

    @Test
    @DisplayName("Opponent casting historic spell does not trigger controller's Teshar")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves graveyardElves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(graveyardElves));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new ShortSword(), "{1}");

        // No graveyard choice prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting a legendary creature triggers Teshar")
    void legendaryCreatureTriggers() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves graveyardElves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(graveyardElves));

        // Cast another legendary creature (Teshar itself is legendary)
        harness.castFromHand(player1, new TesharAncestorsApostle(), "{3}{W}");

        // Should prompt for graveyard target selection
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Casting a Saga returns a creature with mana value exactly three before the Saga resolves")
    void sagaReturnsManaValueThreeCreature() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        AcademyDrake drake = new AcademyDrake();
        harness.setGraveyard(player1, List.of(drake));

        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.handleMultipleCardsChosen(player1, List.of(drake.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Academy Drake");
        harness.assertNotInGraveyard(player1, "Academy Drake");
        harness.assertNotOnBattlefield(player1, "History of Benalia");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A creature in the opponent's graveyard cannot be returned")
    void opponentGraveyardIsNotEligible() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        harness.setGraveyard(player2, List.of(new LlanowarElves()));

        harness.castFromHand(player1, new ShortSword(), "{1}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("A legal graveyard target is mandatory")
    void cannotDeclineTarget() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));
        harness.castFromHand(player1, new ShortSword(), "{1}");

        assertThatThrownBy(
                () -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("The ability does not return a different creature when its target leaves the graveyard")
    void missingTargetDoesNotChooseAnotherCreature() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves elves = new LlanowarElves();
        AcademyDrake drake = new AcademyDrake();
        harness.setGraveyard(player1, List.of(elves, drake));
        harness.castFromHand(player1, new ShortSword(), "{1}");
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.setGraveyard(player1, List.of(drake));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Academy Drake");
        harness.assertInGraveyard(player1, "Academy Drake");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability resolves after Teshar leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        LlanowarElves elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));
        harness.castFromHand(player1, new ShortSword(), "{1}");
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Teshar does not trigger for its own casting")
    void ownCastingDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        harness.castFromHand(player1, new TesharAncestorsApostle(), "{3}{W}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Teshar, Ancestor's Apostle");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Putting a historic permanent onto the battlefield without casting does not trigger")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        harness.enterBattlefieldAndReturn(player1, new ShortSword());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }
}
