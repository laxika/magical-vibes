package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EmrakulTheAeonsTorn;
import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGreatDoor;
import com.github.laxika.magicalvibes.cards.k.KozilekButcherOfTruth;
import com.github.laxika.magicalvibes.cards.o.OakenSiren;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HitTheMotherLode.class, KozilekButcherOfTruth.class, Plains.class,
        OakenSiren.class, EmrakulTheAeonsTorn.class, QuintoriusKand.class, GuardianOfTheGreatDoor.class})
class HitTheMotherLodeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates tapped Treasures equal to the difference from the discovered card's mana value")
    void createsTappedTreasuresBasedOnDiscoveredManaValue() {
        OakenSiren discovered = new OakenSiren();
        harness.setLibrary(player1, List.of(new Plains(), discovered));

        castHitTheMotherLode();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(8);
        assertThat(treasures).allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Creates no Treasures when no qualifying card is found")
    void createsNoTreasuresWhenNoCardIsFound() {
        harness.setLibrary(player1, List.of(new Plains()));

        castHitTheMotherLode();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates no Treasures when the discovered card has mana value ten")
    void createsNoTreasuresWhenDiscoveredCardHasManaValueTen() {
        KozilekButcherOfTruth discovered = new KozilekButcherOfTruth();
        harness.setLibrary(player1, List.of(discovered));

        castHitTheMotherLode();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsTreasuresBeforeTheDiscoveredSpellResolves() {
        OakenSiren discovered = new OakenSiren();
        harness.setLibrary(player1, List.of(discovered));

        castHitTheMotherLode();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Treasure")).hasSize(8)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isTrue());
        assertThat(findPermanents(player1, "Oaken Siren")).isEmpty();
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(discovered));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Oaken Siren")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void discoveredSpellTriggersCastingFromExileAbilities() {
        harness.addToBattlefield(player1, new QuintoriusKand());
        harness.setLibrary(player1, List.of(new OakenSiren()));

        castHitTheMotherLode();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Treasure")).hasSize(8);
    }

    @Test
    void skipsLandsAndExpensiveCardsAndReturnsThemBelowUntouchedCards() {
        Plains land = new Plains();
        EmrakulTheAeonsTorn expensive = new EmrakulTheAeonsTorn();
        OakenSiren discovered = new OakenSiren();
        Plains untouched = new Plains();
        harness.setLibrary(player1, List.of(land, expensive, discovered, untouched));

        castHitTheMotherLode();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).startsWith(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, expensive);
        assertThat(findPermanents(player1, "Treasure")).hasSize(8);
    }

    @Test
    void freeCastingStillRequiresAdditionalCostsBeforeTreasuresAreCreated() {
        List<Permanent> lands = List.of(
                harness.addToBattlefieldAndReturn(player1, new Plains()),
                harness.addToBattlefieldAndReturn(player1, new Plains()),
                harness.addToBattlefieldAndReturn(player1, new Plains()),
                harness.addToBattlefieldAndReturn(player1, new Plains()));
        GuardianOfTheGreatDoor discovered = new GuardianOfTheGreatDoor();
        harness.setLibrary(player1, List.of(discovered));

        castHitTheMotherLode();
        harness.handleCardChosen(player1, 0);

        if (gd.interaction.isAwaitingInput()) {
            assertThat(findPermanents(player1, "Treasure")).isEmpty();
            assertThat(gd.stack).noneSatisfy(entry -> assertThat(entry.getCard()).isSameAs(discovered));
        } else {
            assertThat(lands).allSatisfy(land -> assertThat(land.isTapped()).isTrue());
            assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(discovered));
            assertThat(findPermanents(player1, "Treasure")).hasSize(8);
        }
    }

    @Test
    void emptyLibraryCreatesNoTreasures() {
        harness.setLibrary(player1, List.of());

        castHitTheMotherLode();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void castHitTheMotherLode() {
        harness.setHand(player1, List.of(new HitTheMotherLode()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
