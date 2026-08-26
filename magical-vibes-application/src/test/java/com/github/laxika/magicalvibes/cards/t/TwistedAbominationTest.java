package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwistedAbomination.class, Swamp.class, GrizzlyBears.class})
class TwistedAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Black mana grants Twisted Abomination a regeneration shield")
    void blackManaActivatesRegeneration() {
        Permanent abomination = addCreatureReady(player1, new TwistedAbomination());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(abomination.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Swampcycling searches for a Swamp and discards Twisted Abomination")
    void swampcyclingSearchesForSwamp() {
        TwistedAbomination abomination = new TwistedAbomination();
        harness.setHand(player1, List.of(abomination));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Swamp);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
        harness.assertInGraveyard(player1, "Twisted Abomination");
    }
}
