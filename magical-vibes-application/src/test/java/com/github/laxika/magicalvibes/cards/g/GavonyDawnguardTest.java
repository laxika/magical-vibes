package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DawnhartMentor;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.f.FatefulAbsence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GavonyDawnguard.class, DawnhartRejuvenator.class, Forest.class,
        DawnhartMentor.class, FatefulAbsence.class})
class GavonyDawnguardTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new GavonyDawnguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void findsCreatureWithManaValueThreeOrLessAndLetsYouOrderTheRest() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        Card land = new Forest();
        Card expensiveCreature = new DawnhartRejuvenator();
        Card eligibleCreature = new DawnhartMentor();
        Card otherLand = new Forest();
        harness.setLibrary(player1, List.of(land, expensiveCreature, eligibleCreature, otherLand));
        makeItNight();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleCreature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                remaining.indexOf(otherLand),
                remaining.indexOf(expensiveCreature),
                remaining.indexOf(land))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLand, expensiveCreature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsAllFourCardsOnTheBottomWhenThereIsNoEligibleCreature() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        Card first = new Forest();
        Card second = new DawnhartRejuvenator();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        makeItNight();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                remaining.indexOf(first),
                remaining.indexOf(second),
                remaining.indexOf(third),
                remaining.indexOf(fourth))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringDuringNightDoesNotChangeTheDesignationOrTriggerTheLibraryAbility() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new GavonyDawnguard()));
        Card top = new DawnhartMentor();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void establishingDayDoesNotTriggerTheLibraryAbility() {
        harness.setHand(player1, List.of(new GavonyDawnguard()));
        Card top = new DawnhartMentor();
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineAnEligibleCreatureAndOrdersAllFourBelowUntouchedCards() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        Card eligible = new DawnhartMentor();
        Card noncreature = new FatefulAbsence();
        Card expensive = new DawnhartRejuvenator();
        Card land = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(eligible, noncreature, expensive, land, untouched));
        makeItNight();

        harness.handleCardChosen(player1, -1);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                remaining.indexOf(land), remaining.indexOf(expensive),
                remaining.indexOf(noncreature), remaining.indexOf(eligible))));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, land, expensive, noncreature, eligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void nightBecomingDayFindsAManaValueThreeCreatureInAShortLibrary() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        Card eligible = new DawnhartMentor();
        Card noncreature = new FatefulAbsence();
        harness.setLibrary(player1, List.of(noncreature, eligible));
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotLookBeyondTheTopFourCards() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        Card first = new Forest();
        Card second = new FatefulAbsence();
        Card third = new DawnhartRejuvenator();
        Card fourth = new Forest();
        Card fifth = new DawnhartMentor();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));

        makeItNight();
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                remaining.indexOf(first), remaining.indexOf(second),
                remaining.indexOf(third), remaining.indexOf(fourth))));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, first, second, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTakeTheOnlyCardInTheLibraryWithoutAReorderChoice() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        Card eligible = new DawnhartMentor();
        harness.setLibrary(player1, List.of(eligible));

        makeItNight();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        makeItNight();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void wardCountersAnOpponentsSpellWhenTheyCannotPay() {
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FatefulAbsence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Gavony Dawnguard"));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Gavony Dawnguard");
        harness.assertInGraveyard(player2, "Fateful Absence");
    }

    @Test
    void wardOffersToPayOneManaAndLetsTheSpellResolveWhenPaid() {
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new FatefulAbsence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Gavony Dawnguard"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gavony Dawnguard");
        harness.assertInGraveyard(player1, "Gavony Dawnguard");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void wardDoesNotTriggerForItsControllersSpell() {
        harness.addToBattlefield(player1, new GavonyDawnguard());
        harness.setHand(player1, List.of(new FatefulAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gavony Dawnguard"));

        harness.assertInGraveyard(player1, "Gavony Dawnguard");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void makeItNight() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();
    }
}
