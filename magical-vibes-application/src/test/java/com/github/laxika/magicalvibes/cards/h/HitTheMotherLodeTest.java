package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozilekButcherOfTruth;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HitTheMotherLode.class, GrizzlyBears.class, KozilekButcherOfTruth.class, Plains.class})
class HitTheMotherLodeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates tapped Treasures equal to the difference from the discovered card's mana value")
    void createsTappedTreasuresBasedOnDiscoveredManaValue() {
        GrizzlyBears discovered = new GrizzlyBears();
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

    private void castHitTheMotherLode() {
        harness.setHand(player1, List.of(new HitTheMotherLode()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
