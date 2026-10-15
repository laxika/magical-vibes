package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DiregrafScavenger;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstForDiscovery.class, EvolvingWilds.class, Forest.class, DiregrafScavenger.class,
        Island.class, Mountain.class})
class ThirstForDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and may stop after discarding a basic land")
    void mayStopAfterDiscardingBasicLand() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new ThirstForDiscovery(), new DiregrafScavenger()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires two discards when no basic land is discarded")
    void requiresTwoDiscardsWithoutBasicLand() {
        harness.setLibrary(player1, List.of(new EvolvingWilds(), new DiregrafScavenger(), new DiregrafScavenger()));
        harness.setHand(player1, List.of(new ThirstForDiscovery(), new DiregrafScavenger()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        harness.handleCardChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Evolving Wilds");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May discard a basic land that was in hand before drawing")
    void mayDiscardBasicLandAlreadyInHand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new DiregrafScavenger(), new EvolvingWilds(), new Mountain()));
        harness.setHand(player1, List.of(new ThirstForDiscovery(), forest));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May discard two basic lands instead of stopping after one")
    void mayDiscardTwoBasicLands() {
        Forest forest = new Forest();
        Island island = new Island();
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, island, mountain));
        harness.castFromHand(player1, new ThirstForDiscovery(), "{2}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, island);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May keep a basic land and discard two other cards")
    void mayKeepBasicLandAndDiscardTwoOtherCards() {
        Forest forest = new Forest();
        EvolvingWilds wilds = new EvolvingWilds();
        DiregrafScavenger scavenger = new DiregrafScavenger();
        harness.setLibrary(player1, List.of(forest, wilds, scavenger));
        harness.castFromHand(player1, new ThirstForDiscovery(), "{2}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wilds, scavenger);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot stop after discarding a nonbasic land")
    void cannotStopAfterDiscardingNonbasicLand() {
        EvolvingWilds wilds = new EvolvingWilds();
        DiregrafScavenger scavenger = new DiregrafScavenger();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(wilds, scavenger, forest));
        harness.castFromHand(player1, new ThirstForDiscovery(), "{2}{U}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(scavenger, forest);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wilds, scavenger);
        assertThat(gd.stack).isEmpty();
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}
