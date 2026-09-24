package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SproutingGoblin.class, Forest.class, TropicalIsland.class, GrizzlyBears.class})
class SproutingGoblinTest extends BaseCardTest {

    @Test
    void kickedEntrySearchesForLandWithBasicLandType() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new TropicalIsland(), new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2)
                .anyMatch(TropicalIsland.class::isInstance)
                .anyMatch(Forest.class::isInstance)
                .noneMatch(GrizzlyBears.class::isInstance);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(
                card -> card instanceof TropicalIsland || card instanceof Forest);
    }

    @Test
    void unKickedEntryDoesNotSearch() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertOnBattlefield(player1, "Sprouting Goblin");
    }

    @Test
    void tappingAndSacrificingALandDrawsACard() {
        var goblin = addCreatureReady(player1, new SproutingGoblin());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void abilityCannotBeActivatedWithoutALandToSacrifice() {
        addCreatureReady(player1, new SproutingGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
