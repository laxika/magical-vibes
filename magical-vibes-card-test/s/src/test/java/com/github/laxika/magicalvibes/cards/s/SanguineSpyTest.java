package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrookedCustodian;
import com.github.laxika.magicalvibes.cards.c.CutthroatContender;
import com.github.laxika.magicalvibes.cards.d.DealGoneBad;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Murder;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanguineSpy.class, Forest.class, CrookedCustodian.class, DealGoneBad.class, Island.class, Murder.class, CutthroatContender.class})
class SanguineSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} and sacrifices another creature to surveil 1")
    void sacrificesAnotherCreatureAndSurveils() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addToBattlefield(player1, new CrookedCustodian());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Crooked Custodian");
        harness.assertOnBattlefield(player1, "Sanguine Spy");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Cannot sacrifice Sanguine Spy for its own ability")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At the end step, five distinct graveyard mana values may be paid for a draw")
    void endStepMayPayLifeToDrawWithFiveDistinctManaValues() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("The end-step draw does not trigger with fewer than five distinct graveyard mana values")
    void endStepDoesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues().subList(0, 4));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The end-step draw triggers only during its controller's end step")
    void endStepTriggerDoesNotFireOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void surveilMayKeepTheTopCard() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addToBattlefield(player1, new CrookedCustodian());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        harness.assertInGraveyard(player1, "Crooked Custodian");
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addToBattlefield(player2, new CrookedCustodian());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sanguine Spy");
        harness.assertOnBattlefield(player2, "Crooked Custodian");
    }

    @Test
    void duplicateManaValuesDoNotMeetTheThreshold() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, List.of(new Forest(), new Island(),
                new CutthroatContender(), new CrookedCustodian(), new Murder()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsGraveyardDoesNotCountTowardTheThreshold() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, graveyardWithFiveDistinctManaValues());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void endStepMayDeclineLifePayment() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void cannotPayTwoLifeWithOnlyOneLife() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void thresholdIsCheckedAgainWhenTheTriggerResolves() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues().subList(0, 4));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void surveilWithAnEmptyLibraryStillPaysTheSacrificeCost() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addToBattlefield(player1, new CrookedCustodian());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crooked Custodian");
        harness.assertOnBattlefield(player1, "Sanguine Spy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private List<Card> graveyardWithFiveDistinctManaValues() {
        return List.of(new Forest(), new CutthroatContender(),
                new CrookedCustodian(), new Murder(), new DealGoneBad());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
