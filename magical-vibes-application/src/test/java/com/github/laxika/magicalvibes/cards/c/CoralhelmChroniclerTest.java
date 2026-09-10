package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CoralhelmChronicler.class, AcademyDrake.class, Forest.class, GrizzlyBears.class})
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
}
