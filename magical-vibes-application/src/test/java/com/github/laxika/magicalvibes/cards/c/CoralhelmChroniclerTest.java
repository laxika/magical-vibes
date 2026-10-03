package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoralhelmChronicler.class, AcademyDrake.class, Forest.class, GrizzlyBears.class,
        TazeemRoilmage.class, EverflowingChalice.class})
class CoralhelmChroniclerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked spell draws a card, then discards a card")
    void kickedSpellTriggersLoot() {
        Card kickedSpell = new AcademyDrake();
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new CoralhelmChronicler());
        harness.setHand(player1, List.of(kickedSpell, discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a non-kicked spell does not trigger the loot ability")
    void nonKickedSpellDoesNotTriggerLoot() {
        Card spell = new AcademyDrake();
        Card remaining = new GrizzlyBears();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new CoralhelmChronicler());
        harness.setHand(player1, List.of(spell, remaining));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The enters-the-battlefield ability offers one kicker card from the top five")
    void offersKickerCardFromTopFive() {
        Card first = new GrizzlyBears();
        Card kicker = new AcademyDrake();
        Card third = new Forest();
        Card fourth = new GrizzlyBears();
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(first, kicker, third, fourth, fifth));

        harness.enterBattlefieldAndReturn(player1, new CoralhelmChronicler());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(kicker.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(kicker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(kicker);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, third, fourth, fifth);
    }

    @Test
    @DisplayName("The enters-the-battlefield ability bottoms the top five when no kicker card is found")
    void noKickerCardIsFound() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        Card fourth = new Forest();
        Card fifth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));

        harness.enterBattlefieldAndReturn(player1, new CoralhelmChronicler());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth, fifth);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second, third, fourth, fifth);
    }

    @Test
    @DisplayName("Declining a kicker card bottoms only the top five, preserving deeper cards")
    void mayDeclineKickerCard() {
        Card kicker = new TazeemRoilmage();
        List<Card> topFive = List.of(kicker, new Forest(), new Forest(), new Forest(), new Forest());
        Card sixth = new TazeemRoilmage();
        Card seventh = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), sixth, seventh));

        harness.enterBattlefieldAndReturn(player1, new CoralhelmChronicler());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(kicker.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 7))
                .containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library offers every kicker card but allows taking only one")
    void shortLibraryWithMultipleKickerCards() {
        Card firstKicker = new TazeemRoilmage();
        Card secondKicker = new TazeemRoilmage();
        Card land = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstKicker, land, secondKicker));

        harness.enterBattlefieldAndReturn(player1, new CoralhelmChronicler());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstKicker.getId(), secondKicker.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(secondKicker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondKicker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstKicker, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent casting a kicked spell does not trigger the loot ability")
    void opponentsKickedSpellDoesNotTriggerLoot() {
        Card untouched = new Forest();
        harness.addToBattlefield(player1, new CoralhelmChronicler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(untouched));
        harness.setHand(player2, List.of(new TazeemRoilmage()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        harness.assertOnBattlefield(player2, "Tazeem Roilmage");
    }

    @Test
    @DisplayName("The card drawn by the loot trigger can be discarded")
    void mayDiscardTheDrawnCard() {
        Card remaining = new Forest();
        Card drawn = new Forest();
        harness.addToBattlefield(player1, new CoralhelmChronicler());
        harness.setHand(player1, List.of(new TazeemRoilmage(), remaining));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A card with multikicker is eligible for the enters ability")
    void maySelectMultikickerCard() {
        Card multikicker = new EverflowingChalice();
        Card land = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(multikicker, land));

        harness.enterBattlefieldAndReturn(player1, new CoralhelmChronicler());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(multikicker.getId());
        harness.handleMultipleCardsChosen(player1, List.of(multikicker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(multikicker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }
}
