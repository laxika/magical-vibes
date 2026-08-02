package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChordOfCallingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only creature cards with mana value <= X")
    void presentsOnlyCreaturesWithinManaValueBound() {
        castChord(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Creatures of any color are eligible, non-creatures are not")
    void anyColorCreatureIsEligible() {
        castChord(5);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears", "Air Elemental");
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield")
    void chosenCreatureEntersBattlefield() {
        castChord(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosen));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosen));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chord of Calling goes to the graveyard after resolving")
    void chordGoesToGraveyard() {
        castChord(2);
        setupLibrary();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Chord of Calling");
    }

    @Test
    @DisplayName("X=0 finds nothing when the library has no zero-cost creature")
    void xZeroFindsNothing() {
        castChord(0);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Convoke lets tapped creatures pay for the spell")
    void convokePaysForTheSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChordOfCalling()));
        // {X}{G}{G}{G} with X=1: three green mana plus the convoked Grizzly Bears paying the {1}.
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 1, null, null,
                List.of(), List.of(bears.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chord of Calling");
        assertThat(bears.isTapped()).isTrue();
    }

    private void castChord(int xValue) {
        harness.setHand(player1, List.of(new ChordOfCalling()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 3);
        harness.castInstant(player1, 0, xValue, null);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // Llanowar Elves: MV 1, Grizzly Bears: MV 2, Air Elemental: MV 5, Plains: MV 0 (not a creature).
        deck.addAll(List.of(new LlanowarElves(), new GrizzlyBears(), new AirElemental(), new Plains()));
    }
}
