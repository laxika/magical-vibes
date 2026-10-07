package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraumaticVisions.class, LlanowarElves.class, Plains.class, Forest.class,
        Island.class, GrizzlyBears.class, RuptureSpire.class})
class TraumaticVisionsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the targeted spell, sending it to its owner's graveyard")
    void countersTargetSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new TraumaticVisions()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearches() {
        harness.setHand(player1, List.of(new TraumaticVisions()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traumatic Visions");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    @Test
    @DisplayName("Choosing a basic land from the search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.setHand(player1, List.of(new TraumaticVisions()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(),
                new GrizzlyBears()));
    }

    @Test
    @DisplayName("Landcycling discards as a cost before resolving and does not draw")
    void discardsBeforeResolutionAndDoesNotDraw() {
        TraumaticVisions visions = new TraumaticVisions();
        Island land = new Island();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(visions));
        harness.setLibrary(player1, List.of(creature, land));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Traumatic Visions");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Basic landcycling excludes nonbasic lands and searches only your library")
    void excludesNonbasicLandsAndOpponentsLibrary() {
        Forest forest = new Forest();
        RuptureSpire spire = new RuptureSpire();
        Island opponentsLand = new Island();
        harness.setHand(player1, List.of(new TraumaticVisions()));
        harness.setLibrary(player1, List.of(spire, forest));
        harness.setLibrary(player2, List.of(opponentsLand));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spire);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
    }

    @Test
    @DisplayName("A restricted basic land search may fail to find even when a basic land exists")
    void mayFailToFind() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new TraumaticVisions()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Traumatic Visions");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling resolves without finding a card when no basic lands exist")
    void noMatchingBasicLands() {
        RuptureSpire spire = new RuptureSpire();
        harness.setHand(player1, List.of(new TraumaticVisions()));
        harness.setLibrary(player1, List.of(spire));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spire);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling requires blue mana and cannot discard without paying its cost")
    void requiresBlueMana() {
        TraumaticVisions visions = new TraumaticVisions();
        harness.setHand(player1, List.of(visions));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(visions);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(visions);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters an instant spell so its original target can resolve")
    void countersInstantSpell() {
        LlanowarElves elves = new LlanowarElves();
        TraumaticVisions opponentsCounter = new TraumaticVisions();
        harness.setHand(player1, List.of(elves, new TraumaticVisions()));
        harness.setHand(player2, List.of(opponentsCounter));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, opponentsCounter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Traumatic Visions");
        harness.assertInGraveyard(player1, "Traumatic Visions");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling can resolve with an empty library")
    void emptyLibrary() {
        harness.setHand(player1, List.of(new TraumaticVisions()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traumatic Visions");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
