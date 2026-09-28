package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WilfredMott.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class WilfredMottTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep adds a time counter and looks at that many cards")
    void upkeepAddsCounterAndLooksAtMatchingCards() {
        Permanent wilfred = addReadyWilfred();
        wilfred.setCounterCount(CounterType.TIME, 1);
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(eligible, tooExpensive, instant));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wilfred.getCounterCount(CounterType.TIME)).isEqualTo(2);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(eligible, tooExpensive);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Puts one eligible nonland permanent onto the battlefield")
    void putsEligiblePermanentOntoBattlefield() {
        addReadyWilfred().setCounterCount(CounterType.TIME, 2);
        Card eligible = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(eligible, land, instant));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, instant);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent wilfred = addReadyWilfred();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(wilfred.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyWilfred() {
        return harness.addToBattlefieldAndReturn(player1, new WilfredMott());
    }

}
