package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({GavonyDawnguard.class, ColossalDreadmaw.class, Forest.class, GrizzlyBears.class})
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
        Card expensiveCreature = new ColossalDreadmaw();
        Card eligibleCreature = new GrizzlyBears();
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
        Card second = new ColossalDreadmaw();
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

    private void makeItNight() {
        gd.spellsCastLastTurn.put(player2.getId(), 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
