package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CommandersSphere;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WilfredMott.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        CommandersSphere.class})
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

    @Test
    void mayDeclineAndPutsLookedAtCardsBelowUntouchedLibrary() {
        addReadyWilfred().setCounterCount(CounterType.TIME, 1);
        Card eligible = new CommandersSphere();
        Card land = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(eligible, land, untouched));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Commander's Sphere");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(eligible, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptsNoncreaturePermanentWithManaValueExactlyThree() {
        addReadyWilfred();
        Card sphere = new CommandersSphere();
        harness.setLibrary(player1, List.of(sphere));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sphere.getId()));

        harness.assertOnBattlefield(player1, "Commander's Sphere");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(sphere.getId()) && !p.isTapped());
    }

    @Test
    void cannotChooseMoreThanOneEligibleCard() {
        addReadyWilfred().setCounterCount(CounterType.TIME, 1);
        Card first = new CommandersSphere();
        Card second = new CommandersSphere();
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(countPermanents(player1, "Commander's Sphere")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void noEligibleCardsArePutBelowUntouchedLibraryWithoutAChoice() {
        addReadyWilfred().setCounterCount(CounterType.TIME, 2);
        Card land = new Forest();
        Card expensive = new HillGiant();
        Card instant = new Shock();
        Card untouched = new CommandersSphere();
        harness.setLibrary(player1, List.of(land, expensive, instant, untouched));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(land, expensive, instant);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void looksAtAllRemainingCardsWhenLibraryIsShorterThanCounterCount() {
        Permanent wilfred = addReadyWilfred();
        wilfred.setCounterCount(CounterType.TIME, 5);
        Card eligible = new CommandersSphere();
        harness.setLibrary(player1, List.of(eligible));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        assertThat(wilfred.getCounterCount(CounterType.TIME)).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Commander's Sphere");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryStillAddsCounterWithoutAChoice() {
        Permanent wilfred = addReadyWilfred();
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wilfred.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void usesLastKnownCounterCountWhenWilfredDiesBeforeResolution() {
        Permanent wilfred = addReadyWilfred();
        wilfred.setCounterCount(CounterType.TIME, 2);
        Card eligible = new CommandersSphere();
        Card land = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(eligible, land, untouched));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, wilfred.getId());
        harness.castAndResolveInstant(player2, 0, wilfred.getId());
        harness.assertInGraveyard(player1, "Wilfred Mott");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(eligible, land);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.assertOnBattlefield(player1, "Commander's Sphere");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, land);
    }

}
