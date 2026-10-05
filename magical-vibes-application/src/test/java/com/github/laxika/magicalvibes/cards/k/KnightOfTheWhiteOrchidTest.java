package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HallowedFountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfTheWhiteOrchid.class, Forest.class, Plains.class, ElvishVisionary.class,
        HallowedFountain.class})
class KnightOfTheWhiteOrchidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB tutors a Plains onto the battlefield when an opponent controls more lands")
    void tutorsPlainsWhenOpponentHasMoreLands() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger → may prompt

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains"));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Declining the optional search puts no Plains onto the battlefield")
    void decliningSearchDoesNothing() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger → may prompt

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("No trigger when you control at least as many lands as each opponent")
    void noSearchWhenNotFewerLands() {
        castKnight();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Intervening-if: ability never goes on the stack when land condition is unmet at ETB")
    void interveningIfDoesNotPutTriggerOnStack() {
        castKnight();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell

        // CR 603.4 / Gatherer: ability won't trigger at all unless an opponent has more lands.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Intervening-if: ability does nothing if land counts equalize before resolution")
    void interveningIfFailsAtResolution() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell — trigger on stack
        assertThat(gd.stack).hasSize(1);

        // Equalize lands before the trigger resolves.
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("The searched Plains enters untapped and only one card leaves the library")
    void searchedPlainsEntersUntapped() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        Plains plains = new Plains();
        ElvishVisionary remainingCard = new ElvishVisionary();
        harness.setLibrary(player1, List.of(plains, remainingCard));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(plains);
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.assertNotOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("The search can fail to find even with Plains in the library")
    void mayFailToFindPlains() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the search with no Plains finishes without putting a card onto the battlefield")
    void searchWithNoPlainsFinishes() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        ElvishVisionary remainingCard = new ElvishVisionary();
        harness.setLibrary(player1, List.of(remainingCard));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.assertNotOnBattlefield(player1, "Elvish Visionary");
    }

    @Test
    @DisplayName("Accepting the search with an empty library finishes normally")
    void searchWithEmptyLibraryFinishes() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("A nonbasic Plains can be searched and retains its own enters-tapped ability")
    void searchesNonbasicPlains() {
        castKnight();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new HallowedFountain(), new Forest()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName()).containsExactly("Hallowed Fountain");
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Hallowed Fountain");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Hallowed Fountain"))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castKnight() {
        harness.setHand(player1, List.of(new KnightOfTheWhiteOrchid()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new ElvishVisionary()));
    }
}
