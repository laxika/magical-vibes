package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrambleElemental;
import com.github.laxika.magicalvibes.cards.d.DimirInfiltrator;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({ChordOfCalling.class, ElvesOfDeepShadow.class, DimirInfiltrator.class,
        BrambleElemental.class, Plains.class, ElvishMystic.class, Ornithopter.class})
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
                .containsExactlyInAnyOrder("Elves of Deep Shadow", "Dimir Infiltrator");
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
                .containsExactlyInAnyOrder("Elves of Deep Shadow", "Dimir Infiltrator", "Bramble Elemental");
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

        harness.handleCardChosen(player1, 0);

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
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Chord of Calling");
    }

    @Test
    @DisplayName("The search may fail to find a qualifying creature")
    void mayFailToFindCreature() {
        castChord(2);
        setupLibrary();

        harness.passBothPriorities();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
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
    @DisplayName("Convoke taps an untapped creature to pay for the spell")
    void convokePaysForTheSpell() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new ChordOfCalling()));
        // {X}{G}{G}{G} with X=1: three green mana plus the convoked Elf paying the {1}.
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 1, null, null,
                List.of(), List.of(elves.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chord of Calling");
        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X=0 can put a zero-mana creature onto the battlefield untapped")
    void xZeroFindsZeroManaCreature() {
        castChord(0);
        harness.setLibrary(player1, List.of(new Ornithopter(), new Plains()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Chord of Calling");
    }

    @Test
    @DisplayName("Summoning-sick green creatures can pay all three green symbols with convoke")
    void convokePaysColoredCostWithSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new ChordOfCalling()));
        harness.setLibrary(player1, List.of(new Ornithopter()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(List.of(first, second, third)).allMatch(Permanent::isTapped);
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Chord of Calling");
    }

    @Test
    @DisplayName("A colorless creature cannot convoke a green mana symbol")
    void colorlessConvokeCannotPayGreenCost() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ChordOfCalling()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(thopter.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(thopter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not leave a search choice pending")
    void emptyLibraryResolvesNormally() {
        castChord(3);
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Chord of Calling");
    }

    private void castChord(int xValue) {
        harness.setHand(player1, List.of(new ChordOfCalling()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 3);
        harness.castInstant(player1, 0, xValue, null);
    }

    private void setupLibrary() {
        // Elves of Deep Shadow: MV 1, Dimir Infiltrator: MV 2, Bramble Elemental: MV 5,
        // Plains: MV 0 (not a creature).
        harness.setLibrary(player1, List.of(new ElvesOfDeepShadow(), new DimirInfiltrator(),
                new BrambleElemental(), new Plains()));
    }
}
