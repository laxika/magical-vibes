package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
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

@CardUsed({EncroachingDragonstorm.class, Forest.class, GrizzlyBears.class, Plains.class, ShivanDragon.class})
class EncroachingDragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it offers up to two basic lands to enter tapped")
    void searchesForUpToTwoBasicLands() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(plains, forest, bears));
        harness.setHand(player1, List.of(new EncroachingDragonstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(plains, forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains")
                        || permanent.getCard().getName().equals("Forest"))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Returns to its owner's hand when a Dragon you control enters")
    void returnsWhenAllyDragonEnters() {
        harness.addToBattlefield(player1, new EncroachingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Encroaching Dragonstorm");
        harness.assertInHand(player1, "Encroaching Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when a non-Dragon creature enters")
    void doesNotReturnForNonDragon() {
        harness.addToBattlefield(player1, new EncroachingDragonstorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Encroaching Dragonstorm");
    }

    @Test
    @DisplayName("Can find no lands even when basic lands are available")
    void canChooseZeroLands() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new EncroachingDragonstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Encroaching Dragonstorm");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can stop after one basic land when another is available")
    void canChooseOnlyOneLand() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));
        harness.setHand(player1, List.of(new EncroachingDragonstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest)
                .hasSize(1)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Entry completes when the library contains no basic lands")
    void noBasicLandsInLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player1, List.of(new EncroachingDragonstorm()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Encroaching Dragonstorm");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not return when an opponent's Dragon enters")
    void doesNotReturnForOpponentDragon() {
        harness.addToBattlefield(player1, new EncroachingDragonstorm());
        harness.setHand(player2, List.of(new ShivanDragon()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shivan Dragon");
        harness.assertOnBattlefield(player1, "Encroaching Dragonstorm");
        harness.assertNotInHand(player1, "Encroaching Dragonstorm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A controlled enchantment returns to its owner rather than its controller")
    void returnsToOwnerHand() {
        Permanent dragonstorm = harness.addToBattlefieldAndReturn(player1, new EncroachingDragonstorm());
        gd.stolenCreatures.put(dragonstorm.getId(), player2.getId());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Encroaching Dragonstorm");
        harness.assertInHand(player2, "Encroaching Dragonstorm");
        harness.assertNotInHand(player1, "Encroaching Dragonstorm");
    }
}
